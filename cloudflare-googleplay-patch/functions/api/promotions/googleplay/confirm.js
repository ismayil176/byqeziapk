import { ensureDB, json, requireUser, unauthorized, badRequest } from "../../../_utils.js";
import { getPromotionPlanFromDb } from "../_plans.js";
import { applyPromotion } from "../_apply.js";

const PACKAGE_NAME = "az.byqezi.app";

function b64urlBytes(bytes) {
  let binary = "";
  const arr = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  for (let i = 0; i < arr.length; i++) binary += String.fromCharCode(arr[i]);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function b64urlText(value) {
  return b64urlBytes(new TextEncoder().encode(String(value || "")));
}

function pemToArrayBuffer(pem) {
  const clean = String(pem || "").replace(/-----BEGIN PRIVATE KEY-----/g, "").replace(/-----END PRIVATE KEY-----/g, "").replace(/\s+/g, "");
  const binary = atob(clean);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes.buffer;
}

async function createGoogleAccessToken(env) {
  const raw = env.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON || "";
  if (!raw.trim()) throw new Error("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON secret yoxdur");
  const account = JSON.parse(raw);
  if (!account.client_email || !account.private_key) throw new Error("Google service account JSON düzgün deyil");
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: "RS256", typ: "JWT" };
  const payload = {
    iss: account.client_email,
    scope: "https://www.googleapis.com/auth/androidpublisher",
    aud: "https://oauth2.googleapis.com/token",
    iat: now,
    exp: now + 3600,
  };
  const unsigned = `${b64urlText(JSON.stringify(header))}.${b64urlText(JSON.stringify(payload))}`;
  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToArrayBuffer(account.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(unsigned));
  const assertion = `${unsigned}.${b64urlBytes(signature)}`;
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded;charset=UTF-8" },
    body: new URLSearchParams({ grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer", assertion }),
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok || !result.access_token) throw new Error(result.error_description || "Google access token alınmadı");
  return result.access_token;
}

async function verifyGooglePurchase(env, packageName, productId, token) {
  const accessToken = await createGoogleAccessToken(env);
  const url = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${encodeURIComponent(packageName)}/purchases/products/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(token)}`;
  const response = await fetch(url, { headers: { authorization: `Bearer ${accessToken}` } });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(result.error?.message || "Google Play satınalma yoxlanmadı");
  return { result, accessToken };
}

async function consumeGooglePurchase(accessToken, packageName, productId, token) {
  const url = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${encodeURIComponent(packageName)}/purchases/products/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(token)}:consume`;
  await fetch(url, { method: "POST", headers: { authorization: `Bearer ${accessToken}` } }).catch(() => null);
}

function expectedProductId(service, plan) {
  return `${String(service || "").trim()}_${String(plan || "").trim()}`;
}

function randomPart() {
  const bytes = new Uint8Array(4);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

function successPaymentData(existingData, purchase, productId) {
  let data = {};
  try { data = JSON.parse(existingData || "{}"); } catch (_) { data = {}; }
  return JSON.stringify({
    ...data,
    provider: "google_play",
    product_id: productId,
    google_purchase: purchase,
    verified_at: new Date().toISOString(),
  });
}

export async function onRequestPost(context) {
  const env = context.env || {};
  const db = ensureDB(env);
  const user = await requireUser(context);
  if (!user) return unauthorized("Reklam xidməti almaq üçün kabinetə daxil olun");

  const body = await context.request.json().catch(() => null);
  if (!body) return badRequest("Məlumat oxunmadı");

  const listingId = Number(body.listing_id || body.listingId);
  const service = String(body.service || "").trim();
  const planKey = String(body.plan || "").trim();
  const productId = String(body.product_id || "").trim();
  const purchaseToken = String(body.purchase_token || "").trim();
  const packageName = String(body.package_name || PACKAGE_NAME).replace(/\.debug$/, "");
  const googleOrderId = String(body.order_id || "").trim();

  if (!listingId) return badRequest("Elan ID düzgün deyil");
  if (!purchaseToken) return badRequest("Google Play purchase token yoxdur");
  if (packageName !== PACKAGE_NAME) return badRequest("Package name düzgün deyil");

  const selected = await getPromotionPlanFromDb(db, service, planKey);
  if (!selected) return badRequest("Reklam xidməti və müddəti düzgün seçilməyib");
  if (productId !== expectedProductId(service, planKey)) return badRequest("Google Play product ID uyğun deyil");

  const listing = await db.prepare("SELECT * FROM listings WHERE id = ?").bind(listingId).first();
  if (!listing) return badRequest("Elan tapılmadı");
  if (Number(listing.owner_id) !== Number(user.id)) return unauthorized("Bu elanı reklam etmək üçün icazə yoxdur");
  if (listing.status !== "active") return badRequest("Reklam yalnız aktiv elanlar üçün işləyir");

  const duplicate = await db.prepare(`
    SELECT * FROM promotion_payments
    WHERE (epoint_transaction = ? OR bank_transaction = ?)
      AND service = ? AND plan_key = ?
    LIMIT 1
  `).bind(purchaseToken, googleOrderId || purchaseToken, service, planKey).first().catch(() => null);
  if (duplicate?.status === "success") {
    return json({ ok: true, message: "Reklam paketi artıq aktivləşdirilib", payment: duplicate });
  }

  const verified = await verifyGooglePurchase(env, packageName, productId, purchaseToken);
  const purchase = verified.result || {};
  if (Number(purchase.purchaseState) !== 0) return badRequest("Google Play ödənişi tamamlanmayıb");

  const amount = Number(Number(selected.plan.amount).toFixed(2));
  const orderId = `gplay-${Date.now()}-${listingId}-${randomPart()}`;
  const paymentData = successPaymentData("{}", purchase, productId);

  await db.prepare(`
    INSERT INTO promotion_payments (listing_id, owner_id, service, plan_key, amount, currency, order_id, epoint_transaction, bank_transaction, status, payment_data)
    VALUES (?, ?, ?, ?, ?, 'AZN', ?, ?, ?, 'success', ?)
  `).bind(
    listingId,
    user.id,
    service,
    planKey,
    amount,
    orderId,
    purchaseToken,
    googleOrderId || purchaseToken,
    paymentData
  ).run();

  const payment = await db.prepare("SELECT * FROM promotion_payments WHERE order_id = ?").bind(orderId).first();
  const expiresAt = await applyPromotion(db, payment);
  await consumeGooglePurchase(verified.accessToken, packageName, productId, purchaseToken);

  const updatedListing = await db.prepare("SELECT * FROM listings WHERE id = ? AND owner_id = ?").bind(listingId, user.id).first();
  return json({
    ok: true,
    message: expiresAt ? "Reklam paketi aktivləşdirildi" : "Ödəniş qəbul edildi, amma elan aktiv olmadığı üçün paket tətbiq olunmadı",
    order_id: orderId,
    expires_at: expiresAt,
    listing: updatedListing,
  });
}
