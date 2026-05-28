package az.byqezi.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.Window;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int PURPLE = Color.rgb(92, 16, 172);
    private static final int PURPLE_DARK = Color.rgb(62, 6, 112);
    private static final int BG = Color.rgb(247, 246, 251);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(30, 28, 42);
    private static final int MUTED = Color.rgb(116, 113, 130);
    private static final int LIGHT_MUTED = Color.rgb(168, 166, 179);
    private static final int BORDER = Color.rgb(226, 222, 236);
    private static final int FIELD = Color.rgb(248, 248, 251);
    private static final int VIP_BG = Color.rgb(34, 0, 60);
    private static final int GOLD = Color.rgb(255, 183, 0);
    private static final int REQ_ADD_IMAGES = 7101;
    private static final int MIN_ADD_IMAGES = 3;
    private static final int MAX_ADD_IMAGES = 15;
    private static final String SESSION_PREFS = "byqezi_native_session";
    private static final String PREF_LOGGED_IN = "logged_in";
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_USER_PHONE = "user_phone";
    private static final String PREF_COOKIES = "cookies_json";
    private static final String PREF_PENDING_PROMOTION_ORDER = "pending_promotion_order";
    private static final String PREF_PENDING_PROMOTION_LISTING = "pending_promotion_listing";

    private CookieManager nativeCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ArrayList<Listing> listings = new ArrayList<>();
    private final ArrayList<Listing> premiumListings = new ArrayList<>();
    private final ArrayList<Listing> vipListings = new ArrayList<>();
    private final Map<String, Bitmap> imageCache = new HashMap<>();

    private LinearLayout root;
    private ScrollView scrollView;
    private LinearLayout content;
    private LinearLayout bottomNav;
    private LinearLayout detailContactBar;
    private TextView navHomeLabel;
    private TextView navVipLabel;
    private TextView navAddLabel;
    private TextView navSearchLabel;
    private TextView navCabinetLabel;

    private String activeScreen = "home";
    private String brandQuery = "";
    private String modelQuery = "";
    private String filterCity = "";
    private String filterMinPrice = "";
    private String filterMaxPrice = "";
    private String filterMinYear = "";
    private String filterMaxYear = "";
    private String filterCondition = "";
    private String filterBodyType = "";
    private String filterCurrency = "AZN";
    private String filterMinMileage = "";
    private String filterMaxMileage = "";
    private String filterMinEngine = "";
    private String filterMaxEngine = "";
    private String filterMinPower = "";
    private String filterMaxPower = "";
    private String filterColor = "";
    private String filterFuel = "";
    private String filterDrivetrain = "";
    private String filterTransmission = "";
    private String filterSeats = "";
    private String filterOwners = "";
    private String filterMarket = "";
    private String filterSaleType = "";
    private final ArrayList<String> filterEquipment = new ArrayList<>();
    private final ArrayList<String> filterStates = new ArrayList<>();
    private boolean loadingRemote = false;
    private boolean usingLocalFallback = false;
    private AutoCompleteTextView currentBrandInput;
    private AutoCompleteTextView currentModelInput;
    private boolean suppressBrandWatcher = false;
    private Listing currentDetailListing = null;
    private int detailImageIndex = 0;
    private Dialog activeDialog = null;
    private final Map<String, Boolean> favoriteState = new HashMap<>();
    private boolean userLoggedIn = false;
    private String userName = "";
    private String userPhone = "";
    private String pendingAfterLogin = "cabinet";
    private String otpChallengeId = "";
    private String otpPhone = "";
    private boolean authLoading = false;
    private boolean addSubmitting = false;
    private int systemTopInset = 0;
    private int systemBottomInset = 0;
    private final ArrayList<Listing> userListings = new ArrayList<>();
    private boolean userListingsLoading = false;
    private long userListingsLastFetchMs = 0L;
    private String cabinetFilter = "all";
    private AddListingDraft addDraft = new AddListingDraft();
    private BillingClient billingClient;
    private boolean billingReady = false;
    private boolean promotionLoading = false;
    private PromotionSelection pendingPlayPromotion = null;
    private String pendingPromotionOrderId = "";
    private String pendingPromotionListingId = "";
    private final Map<String, ProductDetails> billingProductDetails = new HashMap<>();
    private final LinkedHashMap<String, PromotionService> promotionServices = new LinkedHashMap<>();

    private static final String[] BRANDS = {
            "Abarth", "Acura", "Aito", "Alfa Romeo", "Anyang Kinland", "AODES",
            "Aprilia", "Arctic Cat", "Asia", "Aston Martin", "ATV", "Audi",
            "Avatr", "Avia", "Baic", "Bajaj", "BAW", "BENDA",
            "Bentley", "Bestune", "BMC", "BMW", "BMW Alpina", "Brilliance",
            "BRP", "Buick", "BYD", "C.Moto", "Cadillac", "Can-Am",
            "CFMOTO", "Champ", "Changan", "Chery", "Chevrolet", "Chrysler",
            "Citroen", "Dacia", "Daewoo", "DAF", "Daihatsu", "Dayun",
            "Denza", "DFSK", "Dnepr", "Dodge", "Dofern", "DongFeng",
            "Ducati", "Falcon", "FAW", "Ferrari", "Fiat", "Ford",
            "Forthing", "Foton", "Freedom", "Gabro", "GAC", "GAZ",
            "Geely", "Genesis", "GMC", "Golden Dragon", "Grandmoto", "GST",
            "GWM (Great Wall Motor)", "Hamtaz", "Haojue", "Harley-Davidson", "Haval", "Honda",
            "Hongqi", "HOWO", "Hummer", "Hyundai", "XCMG", "Xiaomi",
            "XPeng", "IJ", "IM", "Indian Motorcycle", "Infiniti", "Iran Khodro",
            "Isuzu", "Iveco", "iCar", "iHonzda", "JAC", "JAECOO",
            "Jaguar", "Jawa", "Jeep", "JETOUR", "JIM", "Jiangmen",
            "Jianshe", "Jinbei", "Jinlang", "JMC", "KAIYI", "KamAz",
            "Karry", "KAvZ", "Kayo", "Kawasaki", "Keeway", "KG Mobility",
            "Khazar", "Kia", "Kinroad", "KrAZ", "KTM", "Kuba",
            "KYC", "QJMotor", "LADA (VAZ)", "Lamborghini", "Land Rover", "Leapmotor",
            "Lexus", "Li Auto", "Lifan", "Lincoln", "Lotus", "LTI",
            "LuAz", "Lynk & Co", "M-Hero", "MAN", "Maple", "Marshal",
            "Maserati", "MAZ", "Mazda", "Megelli", "Mercedes", "Mercedes-Benz",
            "Mercedes-Maybach", "Mercury", "MG", "Mikilon", "Mini", "Minsk",
            "Mitsubishi", "Mondial", "Moskvich", "Motofino", "Muravey", "MV Agusta",
            "Nama", "Neta", "NIU", "Nio", "Nissan", "Opel",
            "PAZ", "Peugeot", "Plymouth", "Polad", "Polaris", "Polestar",
            "Porsche", "Radar", "RAF", "Ravon", "Renault", "Renault Samsung",
            "RKS", "Roewe", "ROX", "ROX (Polar Stone)", "Rolls-Royce", "Rover",
            "Royal Enfield", "Saipa", "SamAuto", "Saturn", "Scania", "Scion",
            "SEAT", "Seres Aito", "Shacman", "Shaolin", "Sharmax", "Skoda",
            "Skyline", "Skywell", "Smart", "Soueast", "Ssang Yong", "Subaru",
            "Suzuki", "SYM", "Tatra", "Temsa", "Tesla", "Tofas",
            "Toyota", "Triumph", "Tufan", "Tur motors", "TVS", "UAZ",
            "Ural", "VGV", "Victoria", "Vmoto", "Voge", "Volkswagen",
            "Volta", "Volvo", "Voyah", "Yadea", "Yamaha", "ZAZ",
            "Zaza", "ZEEKR", "ZX Auto", "ZIL", "Zonsen", "Zontes",
            "Wuling", "BAIC", "Jetour", "Zeekr"
    };

    private static String[] modelList(String joined) {
        return String.valueOf(joined == null ? "" : joined).split("\\|");
    }

    private static String[] buildYearOptions() {
        ArrayList<String> years = new ArrayList<>();
        for (int year = 2026; year >= 1950; year--) years.add(String.valueOf(year));
        return years.toArray(new String[0]);
    }

    private static final String[] YEAR_OPTIONS = buildYearOptions();

    private static final Map<String, String[]> MODELS = new HashMap<String, String[]>() {{
        put("Abarth", modelList("124 Spider|500|595|695|Digər|Grande Punto|Punto Evo"));
        put("Acura", modelList("CL|Digər|ILX|Integra|MDX|NSX|RDX|RL|RLX|RSX|TL|TLX|TSX|ZDX"));
        put("Aito", modelList("Digər|M5|M7|M8|M9"));
        put("Alfa Romeo", modelList("145|146|147|156|159|166|4C|Digər|Giulia|Giulietta|GT|GTV|MiTo|Spider|Stelvio|Tonale"));
        put("Anyang Kinland", modelList("Digər"));
        put("AODES", modelList("Digər"));
        put("Aprilia", modelList("Digər"));
        put("Arctic Cat", modelList("Digər"));
        put("Asia", modelList("Digər"));
        put("Aston Martin", modelList("DB11|DB12|DB7|DB9|DBX|DBS|Digər|Rapide|Vanquish|Vantage|Virage"));
        put("ATV", modelList("Digər"));
        put("Audi", modelList("100|200|80|90|A1|A2|A3|A4|A4 Allroad|A5|A6|A6 Allroad|A7|A8|Cabriolet|Digər|e-tron|e-tron GT|Q2|Q3|Q4 e-tron|Q5|Q7|Q8|R8|RS Q3|RS Q8|RS3|RS4|RS5|RS6|RS7|S3|S4|S5|S6|S7|S8|SQ5|SQ7|SQ8|TT|TTS"));
        put("Avatr", modelList("07|11|12|Digər"));
        put("Avia", modelList("Digər"));
        put("Baic", modelList("A1|A5|Beijing X3|Beijing X5|Beijing X7|BJ20|BJ30|BJ40|BJ60|D20|Digər|EU5|X25|X35|X55|X65|X7|Senova D50|U5 Plus"));
        put("Bajaj", modelList("Boxer|CT|Digər|Dominar|Qute|Pulsar|RE|V15"));
        put("BAW", modelList("Ace|BJ212|Calorie|Digər|Fenix|M7|Pony"));
        put("BENDA", modelList("Digər"));
        put("Bentley", modelList("Arnage|Azure|Bentayga|Brooklands|Continental|Continental Flying Spur|Continental GT|Digər|Flying Spur|Mulsanne"));
        put("Bestune", modelList("B30|B50|B70|B90|Digər|E01|X40|X80|M9|NAT|T33|T55|T77|T90|T99"));
        put("BMC", modelList("Digər"));
        put("BMW", modelList("114|116|118|120|123|125|128|130|135|140|216|218|220|225|228|230|235|240|316|318|320|323|325|328|330|335|340|418|420|425|428|430|435|440|518|520|523|525|528|530|535|540|545|550|630|635|640|645|650|725|728|730|735|740|745|750|760|840|850|Digər|X1|X2|X3|X4|X5|X6|X7|XM|i3|i4|i5|i7|i8|iX|iX1|iX3|M2|M3|M4|M5|M6|M8|Z3|Z4|Z8"));
        put("BMW Alpina", modelList("B3|B4|B5|B6|B7|B8|D3|D4|D5|Digər|XB7"));
        put("Brilliance", modelList("BS2|BS4|BS6|Digər|FRV|H230|H330|H530|M1|M2|V3|V5"));
        put("BRP", modelList("Digər"));
        put("Buick", modelList("Century|Digər|Enclave|Encore|Encore GX|Envision|GL8|LaCrosse|LeSabre|Lucerne|Park Avenue|Regal|Rendezvous|Verano"));
        put("BYD", modelList("Atto 2|Atto 3|Chazor|D1|Denza D9|Destroyer 05|Digər|Dolphin|Dolphin Mini|e1|e2|E5|F0|F3|F6|F7|Frigate 07|Han|Han L|Qin|Qin L|Qin Plus|Qin Plus DM-i|M6|Seagull|Seal|Seal 05|Seal 06|Seal 06 DM-i Touring|Seal 07|Seal U|Shark 6|Song L|Song Max|Song Plus|Song Plus DM-i|Song Plus DM-İ|Song Plus EV|Song Pro|Song Pro DM-i|Tang|Tang DM-i|Yuan|Yuan Plus|Yuan Up"));
        put("C.Moto", modelList("Digər"));
        put("Cadillac", modelList("ATS|BLS|CT4|CT5|CT6|CTS|DeVille|Digər|DTS|Eldorado|Escalade|Fleetwood|XLR|XT4|XT5|XT6|XTS|Seville|SRX|STS"));
        put("Can-Am", modelList("Commander|Defender|Digər|Maverick|Outlander|Renegade|Ryker|Spyder"));
        put("CFMOTO", modelList("150NK|250NK|300NK|300SR|450MT|450SR|650GT|650NK|700CL-X|800MT|Digər|UForce|ZForce"));
        put("Champ", modelList("Digər"));
        put("Changan", modelList("Alsvin|BenBen|CS15|CS35|CS35 Plus|CS55|CS55 Plus|CS75|CS75 Plus|CS85|CS95|Deepal G318|Deepal L07|Deepal S05|Deepal S07|Digər|Eado|F70 (Hunter)|Honor|Hunter|K50 (Hunter)|Qiyuan A05|Qiyuan A06|Qiyuan A07|Qiyuan E07|Qiyuan Q05|Qiyuan Q07|Lamore|Lumin|Nevo A05|Nevo A07|Nevo E07|Nevo Q05|Nevo Q07|Oshan X5|Oshan X7|Raeton|UNI-K|UNI-T|UNI-V|Uni-K|Uni-T|Uni-V|Uni-Z"));
        put("Chery", modelList("Amulet (A15)|Arrizo 3|Arrizo 5|Arrizo 5 Plus|Arrizo 6|Arrizo 8|Bonus (A13)|Digər|E5|Fulwin 2|Fulwin A8|Fulwin T6|Fulwin T9|QQ|Omoda 5|Tiggo|Tiggo 2|Tiggo 2 Pro|Tiggo 3|Tiggo 4|Tiggo 4 Pro|Tiggo 5|Tiggo 7|Tiggo 7 Pro|Tiggo 7 Pro Max|Tiggo 8|Tiggo 8 Pro|Tiggo 8 Pro e+|Tiggo 8 Pro Max|Tiggo 9|Tiggo 9 Pro"));
        put("Chevrolet", modelList("Alero|Astro|Avalanche|Aveo|Blazer|Bolt|Camaro|Caprice|Captiva|Cobalt|Colorado|Corvette|Cruze|Digər|Express|Equinox|Epica|Impala|Lacetti|Lumina|Malibu|Matiz|Nexia|Niva|Orlando|Rezzo|Silverado|Spark|Suburban|Tahoe|Tracker|Trax|Trailblazer|Traverse|Volt"));
        put("Chrysler", modelList("200|300|300C|Aspen|Concorde|Crossfire|Digər|Grand Voyager|Neon|Pacifica|PT Cruiser|Sebring|Town & Country|Voyager"));
        put("Citroen", modelList("Berlingo|C-Crosser|C-Elysee|C1|C2|C3|C3 Aircross|C3 Picasso|C4|C4 Aircross|C4 Cactus|C4 Picasso|C4X|C5|C5 Aircross|C6|C8|Digər|DS3|DS4|DS5|Jumper|Jumpy|SpaceTourer"));
        put("Dacia", modelList("Digər|Dokker|Duster|Jogger|Lodgy|Logan|Logan MCV|Sandero|Solenza|Spring"));
        put("Daewoo", modelList("Damas|Digər|Espero|Gentra|Kalos|Lacetti|Lanos|Leganza|Magnus|Matiz|Nexia|Nubira|Tacuma|Tico"));
        put("DAF", modelList("CF|Digər|XF|XG|XG+|LF"));
        put("Daihatsu", modelList("Applause|Charade|Copen|Cuore|Digər|Materia|Mira|Rocky|Sirion|Terios|YRV"));
        put("Dayun", modelList("Digər"));
        put("Denza", modelList("D9|Digər|N7|N8|Z9|Z9 GT"));
        put("DFSK", modelList("C31|C32|C35|Digər|Fengon 500|Fengon 560|Fengon 580|Glory 330|Glory 370|Seres 3|Seres 5"));
        put("Dnepr", modelList("Digər"));
        put("Dodge", modelList("Avenger|Caliber|Caravan|Challenger|Charger|Dakota|Dart|Digər|Durango|Grand Caravan|Journey|Magnum|Neon|Nitro|Ram|Viper"));
        put("Dofern", modelList("Digər"));
        put("DongFeng", modelList("Aeolus|Aeolus A30|Aeolus AX7|Aeolus E70|Aeolus Haoji|Aeolus Shine|Aeolus Shine GS|AX4|AX7|Digər|Fengdu MX6|Fengguang 330|Fengguang 500|Fengguang 580|Fengon 500|Fengon 580|M5EV|Rich|S30"));
        put("Ducati", modelList("Diavel|Digər|Hypermotard|Monster|Multistrada|Panigale|Scrambler|Streetfighter|Supersport"));
        put("Falcon", modelList("Digər"));
        put("FAW", modelList("Besturn B30|Besturn B50|Besturn B70|Besturn X40|Besturn X80|Digər|Xiali|Jiefang|Oley|Senia R7|Vita"));
        put("Ferrari", modelList("296|360|458|488|812|California|Digər|F12|F8|FF|Portofino|Purosangue|Roma|SF90"));
        put("Fiat", modelList("124|500|500X|500L|Albea|Bravo|Digər|Doblo|Ducato|Fiorino|Freemont|Grande Punto|Linea|Palio|Panda|Punto|Scudo|Stilo|Tipo"));
        put("Ford", modelList("B-Max|Bronco|C-Max|Contour|Cougar|Courier|Digər|EcoSport|Edge|Expedition|Explorer|Escape|Escort|F-150|F-250|Fiesta|Flex|Focus|Fusion|Galaxy|Ka|Kuga|Maverick|Mondeo|Mustang|Puma|Ranger|S-Max|Taurus|Tourneo Connect|Tourneo Custom|Transit|Transit Connect|Transit Custom"));
        put("Forthing", modelList("Digər|Friday|Lingzhi|M4|S50|T5|T5 EVO|U-Tour|Yacht"));
        put("Foton", modelList("Auman|Aumark|Digər|Forland|Gratour|Ollin|Sauvana|Toano|Tunland|View"));
        put("Freedom", modelList("Digər"));
        put("Gabro", modelList("Digər"));
        put("GAC", modelList("Aion LX|Aion S|Aion V|Aion Y|Digər|Emkoo|Empow|GA4|GA5|GA6|GA8|GS3|GS4|GS5|GS8|M6|M8|Trumpchi E8"));
        put("GAZ", modelList("21|24|2705|3102|3110|3302|3307|3309|66|Digər|Gazelle|Gazelle Next|SOBOL|Valday|Volga"));
        put("Geely", modelList("Atlas|Atlas Pro|Azkarra|Binrui|Boyue|Coolray|Digər|Emgrand|Emgrand EC7|Galaxy E5|Galaxy L6|Galaxy L7|Geometry C|Geometry E|X7|Xingyue L|Monjaro|Okavango|Preface|Tugella"));
        put("Genesis", modelList("Digər|G70|G80|G90|GV60|GV70|GV80"));
        put("GMC", modelList("Acadia|Canyon|Digər|Envoy|Savana|Sierra|Terrain|Yukon"));
        put("Golden Dragon", modelList("Digər"));
        put("Grandmoto", modelList("Digər"));
        put("GST", modelList("Digər"));
        put("GWM (Great Wall Motor)", modelList("Cannon|Deer|Digər|Haval H6|Hover|Ora|Ora 03|Ora 07|Poer|Safe|Tank 300|Tank 500|Voleex C30|Wingle|Wingle 5|Wingle 7"));
        put("Hamtaz", modelList("Digər"));
        put("Haojue", modelList("Digər"));
        put("Harley-Davidson", modelList("Breakout|Digər|Fat Bob|Fat Boy|Forty-Eight|Iron 883|Nightster|Road Glide|Road King|Sportster|Street Bob|Street Glide|V-Rod"));
        put("Haval", modelList("Big Dog|Cool Dog|Dargo|Digər|F7|F7x|H2|H4|H5|H6|H6 HEV|H7|H8|H9|Xialong|Jolion|Jolion HEV|Jolion Pro|M6|Raptor"));
        put("Honda", modelList("Accord|City|Civic|Clarity|CR-V|CR-Z|Crosstour|Digər|Element|Elysion|Fit|FR-V|Freed|HR-V|Insight|Inspire|Jazz|Legend|Odyssey|Passport|Pilot|Prelude|Ridgeline|S2000|Shuttle|StepWGN|Vezel"));
        put("Hongqi", modelList("Digər|E-HS3|E-HS9|E-QM5|H5|H6|H7|H9|HQ9|HS3|HS5|HS7|LS7"));
        put("HOWO", modelList("A7|Digər|Hohan|T5G|TX|TX7"));
        put("Hummer", modelList("Digər|EV|H1|H2|H3"));
        put("Hyundai", modelList("Accent|Atos|Azera|Bayon|Coupe|Creta|Digər|Equus|Elantra|Galloper|Genesis|Getz|Grandeur|H-1|H100|IONIQ|IONIQ 5|IONIQ 6|i10|i20|i30|i40|ix20|ix35|ix55|Kona|Matrix|Palisade|Porter|Santa Cruz|Santa Fe|Solaris|Sonata|Starex|Staria|Terracan|Trajet|Tucson|Veloster|Venue|Veracruz"));
        put("XCMG", modelList("Digər"));
        put("Xiaomi", modelList("Digər|SU7|SU7 Max|SU7 Pro|YU7"));
        put("XPeng", modelList("Digər|G3|G6|G9|X9|Mona M03|P5|P7|P7+"));
        put("IJ", modelList("Digər"));
        put("IM", modelList("Digər"));
        put("Indian Motorcycle", modelList("Digər"));
        put("Infiniti", modelList("Digər|EX25|EX35|EX37|FX35|FX37|FX45|FX50|G25|G35|G37|I30|I35|JX35|Q30|Q40|Q45|Q50|Q60|Q70|QX30|QX4|QX50|QX55|QX56|QX60|QX70|QX80|M25|M35|M37|M45"));
        put("Iran Khodro", modelList("Digər"));
        put("Isuzu", modelList("D-Max|Digər|Elf|F-Series|Gemini|Midi|MU-X|N-Series|Rodeo|Trooper"));
        put("Iveco", modelList("Daily|Digər|EuroCargo|Stralis|Trakker"));
        put("iCar", modelList("Digər"));
        put("iHonzda", modelList("Digər"));
        put("JAC", modelList("Digər|iEV7S|J4|J5|J6|J7|JS2|JS3|JS4|JS6|JS8|M3|Refine|S2|S3|S4|S5|S7|T6|T8|T9"));
        put("JAECOO", modelList("Digər|J5|J6|J7|J7 PHEV|J8"));
        put("Jaguar", modelList("Digər|E-Pace|F-Pace|F-Type|X-Type|XE|XF|XJ|XK|I-Pace|S-Type"));
        put("Jawa", modelList("Digər"));
        put("Jeep", modelList("Avenger|Cherokee|Commander|Compass|Digər|Gladiator|Grand Cherokee|Liberty|Patriot|Renegade|Wrangler"));
        put("JETOUR", modelList("Dashing|Digər|X50|X70|X70 Plus|X70 Pro|X70S|X90|X90 Plus|X95|T1|T2|Traveller"));
        put("JIM", modelList("Digər"));
        put("Jiangmen", modelList("Digər"));
        put("Jianshe", modelList("Digər"));
        put("Jinbei", modelList("Digər"));
        put("Jinlang", modelList("Digər"));
        put("JMC", modelList("Baodian|Boarding|Carry|Digər|Vigus|Yuhu"));
        put("KAIYI", modelList("Digər|E5|X3|X3 Pro|X7|X7 Kunlun"));
        put("KamAz", modelList("43118|5320|5410|5511|65115|6520|65201|65206|Digər|K5"));
        put("Karry", modelList("Digər|K50|K60|Q22|Youjin"));
        put("KAvZ", modelList("Digər"));
        put("Kayo", modelList("Digər"));
        put("Kawasaki", modelList("Digər|ER-6n|KLE|KLR|Ninja|Versys|Vulcan|Z1000|Z650|Z750|Z800|Z900|ZX-10R|ZX-6R"));
        put("Keeway", modelList("Digər"));
        put("KG Mobility", modelList("Actyon|Digər|XLV|Korando|Kyron|Musso|Rexton|Tivoli|Torres"));
        put("Khazar", modelList("Digər"));
        put("Kia", modelList("Avella|Cadenza|Carens|Carnival|Ceed|Cerato|Clarus|Digər|EV3|EV4|EV5|EV6|EV9|Forte|K3|K4|K5|K7|K8|K9|Quoris|Magentis|Mohave|Morning|Niro|Opirus|Optima|Pegas|Picanto|Ray|Rio|Seltos|Sephia|Shuma|Sorento|Soul|Spectra|Sportage|Stinger|Stonic|Telluride"));
        put("Kinroad", modelList("Digər"));
        put("KrAZ", modelList("Digər"));
        put("KTM", modelList("125 Duke|200 Duke|250 Duke|390 Duke|690 Duke|790 Duke|890 Duke|Adventure|Digər|EXC|RC 390"));
        put("Kuba", modelList("Digər"));
        put("KYC", modelList("Digər"));
        put("QJMotor", modelList("Digər|SRK 400|SRK 600|SRT 600|SRT 700|SRT 800|SRV 300"));
        put("LADA (VAZ)", modelList("1111 Oka|2101|2102|2103|2104|2105|2106|2107|2108|2109|2110|2111|2112|2113|2114|2115|4x4|Digər|Granta|XRAY|Kalina|Largus|Niva|Niva Travel|Priora|Samara|Vesta"));
        put("Lamborghini", modelList("Aventador|Countach|Diablo|Digər|Gallardo|Huracan|Murcielago|Revuelto|Urus"));
        put("Land Rover", modelList("Defender|Digər|Discovery|Discovery Sport|Freelander|Range Rover|Range Rover Evoque|Range Rover Sport|Range Rover Velar"));
        put("Leapmotor", modelList("B01|B10|C01|C10|C11|C16|Digər|T03"));
        put("Lexus", modelList("CT|Digər|ES|GX|GS|HS|IS|LC|LX|LM|LS|NX|RC|RX|RZ|SC|UX"));
        put("Li Auto", modelList("Digər|L6|L7|L8|L9|Mega"));
        put("Lifan", modelList("320|330|520|530|620|630|650|Digər|X50|X60|X70|X80|Solano"));
        put("Lincoln", modelList("Aviator|Continental|Corsair|Digər|MKC|MKX|MKS|MKT|MKZ|Nautilus|Navigator|Town Car|Zephyr"));
        put("Lotus", modelList("Digər|Exige|Eletre|Elise|Emeya|Emira|Esprit|Evora"));
        put("LTI", modelList("Digər"));
        put("LuAz", modelList("Digər"));
        put("Lynk & Co", modelList("01|02|03|05|06|07|08|09|900|Digər"));
        put("M-Hero", modelList("917|Digər"));
        put("MAN", modelList("Digər|F2000|L2000|TGA|TGE|TGX|TGL|TGM|TGS"));
        put("Maple", modelList("Digər"));
        put("Marshal", modelList("Digər"));
        put("Maserati", modelList("3200 GT|Digər|Ghibli|GranCabrio|GranTurismo|Grecale|Quattroporte|Levante|MC20"));
        put("MAZ", modelList("4370|5336|5440|5516|6312|6501|Digər"));
        put("Mazda", modelList("2|3|323|5|6|626|BT-50|CX-3|CX-30|CX-5|CX-50|CX-60|CX-7|CX-8|CX-9|CX-90|Demio|Digər|MX-30|MX-5|Millenia|MPV|Premacy|RX-7|RX-8|Tribute"));
        put("Megelli", modelList("Digər"));
        put("Mercedes", modelList("190|200|220|230|240|250|260|270|280|300|320|350|400|500|A 140|A 160|A 170|A 180|A 200|A 220|A 250|B 150|B 170|B 180|B 200|B 220|C 180|C 200|C 220|C 230|C 240|C 250|C 270|C 280|C 300|C 350|CLA 180|CLA 200|CLA 250|CLC 180|CLK 200|CLK 240|CLK 320|CLS 350|CLS 500|Digər|E 200|E 220|E 230|E 240|E 250|E 270|E 280|E 300|E 320|E 350|E 400|E 500|G 320|G 350|G 500|G 55 AMG|G 63 AMG|GL 320|GL 350|GL 500|GLA 200|GLA 250|GLB 200|GLC 200|GLC 250|GLC 300|GLE 350|GLE 400|GLE 450|GLE 53 AMG|GLK 250|GLK 280|GLK 300|GLK 350|GLS 450|GLS 580|X 250|ML 270|ML 320|ML 350|R 350|S 320|S 350|S 400|S 500|S 560|S 580|S 600|SL 500|SLK 200|Sprinter|Viano|Vito"));
        put("Mercedes-Benz", modelList("190|200|220|230|240|250|260|270|280|300|320|350|400|500|A 140|A 160|A 170|A 180|A 200|A 220|A 250|B 150|B 170|B 180|B 200|B 220|C 180|C 200|C 220|C 230|C 240|C 250|C 270|C 280|C 300|C 350|CLA 180|CLA 200|CLA 250|CLC 180|CLK 200|CLK 240|CLK 320|CLS 350|CLS 500|Digər|E 200|E 220|E 230|E 240|E 250|E 270|E 280|E 300|E 320|E 350|E 400|E 500|G 320|G 350|G 500|G 55 AMG|G 63 AMG|GL 320|GL 350|GL 500|GLA 200|GLA 250|GLB 200|GLC 200|GLC 250|GLC 300|GLE 350|GLE 400|GLE 450|GLE 53 AMG|GLK 250|GLK 280|GLK 300|GLK 350|GLS 450|GLS 580|X 250|ML 270|ML 320|ML 350|R 350|S 320|S 350|S 400|S 500|S 560|S 580|S 600|SL 500|SLK 200|Sprinter|Viano|Vito"));
        put("Mercedes-Maybach", modelList("Digər|GLS 600|S 560|S 580|S 600|S 650|S 680"));
        put("Mercury", modelList("Digər"));
        put("MG", modelList("3|4|5|6|7|Cyberster|Digər|EHS|HS|Marvel R|MG One|Mulan|One|RX5|ZS|ZS EV"));
        put("Mikilon", modelList("Digər"));
        put("Mini", modelList("Cabrio|Clubman|Cooper|Cooper S|Countryman|Coupe|Digər|Hatch|One|Paceman|Roadster"));
        put("Minsk", modelList("Digər"));
        put("Mitsubishi", modelList("3000GT|ASX|Attrage|Carisma|Colt|Digər|Eclipse|Eclipse Cross|Endeavor|Galant|Grandis|i-MiEV|L200|Lancer|Mirage|Montero|Outlander|Pajero|Pajero IO|Pajero Pinin|Pajero Sport|Space Star"));
        put("Mondial", modelList("Digər"));
        put("Moskvich", modelList("2140|2141|3|3e|412|6|8|Digər"));
        put("Motofino", modelList("Digər"));
        put("Muravey", modelList("Digər"));
        put("MV Agusta", modelList("Digər"));
        put("Nama", modelList("Digər"));
        put("Neta", modelList("AYA|Digər|GT|X|L|S|U|V"));
        put("NIU", modelList("Digər"));
        put("Nio", modelList("Digər|EC6|EC7|EL6|EL7|EL8|ES6|ES7|ES8|ET5|ET5 Touring|ET7|ET9"));
        put("Nissan", modelList("100NX|200SX|350Z|370Z|Almera|Altima|Ariya|Armada|Bluebird|Cedric|Digər|Frontier|GT-R|X-Terra|X-Trail|Juke|Kicks|Qashqai|Quest|Leaf|Maxima|Micra|Murano|Navara|Note|NV200|Pathfinder|Patrol|Primera|Rogue|Sentra|Silvia|Skyline|Sunny|Teana|Terrano|Tiida|Titan|Versa"));
        put("Opel", modelList("Adam|Agila|Ampera|Antara|Ascona|Astra|Calibra|Combo|Corsa|Crossland|Digər|Frontera|Grandland|Insignia|Kadett|Meriva|Mokka|Monterey|Movano|Omega|Rekord|Senator|Signum|Sintra|Tigra|Vectra|Vivaro|Zafira"));
        put("PAZ", modelList("Digər"));
        put("Peugeot", modelList("106|107|108|2008|205|206|207|208|3008|301|306|307|308|4007|4008|405|406|407|408|5008|508|607|807|Bipper|Boxer|Digər|Expert|Partner|Rifter|Traveller"));
        put("Plymouth", modelList("Digər"));
        put("Polad", modelList("Digər"));
        put("Polaris", modelList("Digər|General|Ranger|RZR|Scrambler|Sportsman|Trail Boss"));
        put("Polestar", modelList("1|2|3|4|Digər"));
        put("Porsche", modelList("356|718 Boxster|718 Cayman|911|911 Carrera|911 Carrera S|911 Turbo|924|928|944|Boxster|Cayenne|Cayman|Digər|Macan|Panamera|Taycan"));
        put("Radar", modelList("Digər"));
        put("RAF", modelList("Digər"));
        put("Ravon", modelList("Digər|Gentra|Matiz|Nexia R3|R2|R4"));
        put("Renault", modelList("Arkana|Austral|Captur|Clio|Digər|Dokker|Duster|Espace|Fluence|Kangoo|Kaptur|Koleos|Laguna|Latitude|Logan|Master|Megane|Sandero|Scenic|Symbol|Taliant|Talisman|Tondar|Trafic|Twingo|Vel Satis"));
        put("Renault Samsung", modelList("Digər"));
        put("RKS", modelList("Digər"));
        put("Roewe", modelList("350|550|750|Digər|Ei5|i5|i6|RX3|RX5|RX8"));
        put("ROX", modelList("01|Digər"));
        put("ROX (Polar Stone)", modelList("01|Digər"));
        put("Rolls-Royce", modelList("Cullinan|Dawn|Digər|Ghost|Phantom|Spectre|Wraith"));
        put("Rover", modelList("200|25|400|45|600|75|800|Digər"));
        put("Royal Enfield", modelList("Digər"));
        put("Saipa", modelList("111|131|132|141|151|Digər|Quick|Pride|Saina|Shahin|Tiba"));
        put("SamAuto", modelList("Digər"));
        put("Saturn", modelList("Digər"));
        put("Scania", modelList("Digər|G-series|L-series|P-series|R-series|S-series"));
        put("Scion", modelList("Digər|FR-S|xA|xB|xD|iA|iQ|iM|tC"));
        put("SEAT", modelList("Alhambra|Altea|Arona|Ateca|Cordoba|Digər|Exeo|Ibiza|Leon|Tarraco|Toledo"));
        put("Seres Aito", modelList("Digər|M5|M7|M8|M9"));
        put("Shacman", modelList("Digər|F2000|F3000|X3000|X5000|L3000|M3000"));
        put("Shaolin", modelList("Digər"));
        put("Sharmax", modelList("Digər"));
        put("Skoda", modelList("Citigo|Digər|Enyaq|Fabia|Felicia|Kamiq|Karoq|Kodiaq|Octavia|Rapid|Roomster|Scala|Superb|Yeti"));
        put("Skyline", modelList("Digər"));
        put("Skywell", modelList("Digər|ET5|HT-i|K|Skyhome"));
        put("Smart", modelList("#1|#3|Digər|Forfour|Fortwo|Roadster"));
        put("Soueast", modelList("DX3|DX5|DX7|Digər|S06|S07|S09|V5"));
        put("Ssang Yong", modelList("Actyon|Chairman|Digər|XLV|Korando|Kyron|Musso|Rexton|Rodius|Tivoli|Torres"));
        put("Subaru", modelList("Ascent|B9 Tribeca|Baja|BRZ|Crosstrek|Digər|Forester|XV|Impreza|Justy|Legacy|Levorg|Outback|Tribeca|WRX"));
        put("Suzuki", modelList("Alto|Baleno|Celerio|Digər|Grand Vitara|Ignis|Jimny|Kizashi|Liana|S-Cross|SX4|Splash|Swift|Vitara|Wagon R"));
        put("SYM", modelList("Digər"));
        put("Tatra", modelList("Digər"));
        put("Temsa", modelList("Digər"));
        put("Tesla", modelList("Cybertruck|Digər|Model 3|Model X|Model S|Model Y|Roadster"));
        put("Tofas", modelList("Digər"));
        put("Toyota", modelList("4Runner|Alphard|Auris|Avalon|Avensis|bZ4X|C-HR|Camry|Carina|Celica|Corolla|Corolla Cross|Corona|Crown|Digər|FJ Cruiser|Fortuner|Harrier|Hiace|Highlander|Hilux|Land Cruiser|Land Cruiser Prado|Mark X|Mirai|Prius|ProAce|RAV4|Sequoia|Sienna|Supra|Tacoma|Tundra|Venza|Verso|Vitz|Yaris|Yaris Cross"));
        put("Triumph", modelList("Bonneville|Daytona|Digər|Rocket|Scrambler|Speed Triple|Street Triple|Tiger|Trident"));
        put("Tufan", modelList("Digər"));
        put("Tur motors", modelList("Digər"));
        put("TVS", modelList("Apache|Digər|Jupiter|Ntorq|Raider|Ronin|Sport|Star City"));
        put("UAZ", modelList("2206|3151|3303|3909|452|469|Digər|Hunter|Patriot|Pickup|Profi"));
        put("Ural", modelList("Digər"));
        put("VGV", modelList("Digər"));
        put("Victoria", modelList("Digər"));
        put("Vmoto", modelList("Digər"));
        put("Voge", modelList("300AC|300R|500AC|500DS|525DSX|650DSX|Digər"));
        put("Volkswagen", modelList("Amarok|Arteon|Atlas|Beetle|Bora|Caddy|CC|Crafter|Digər|Eos|Fox|Golf|Golf Plus|ID.3|ID.4|ID.5|ID.6|Jetta|Lupo|Multivan|Passat|Passat CC|Phaeton|Pointer|Polo|Scirocco|Sharan|T-Cross|T-Roc|Taos|Teramont|Tiguan|Touareg|Touran|Transporter|Vento"));
        put("Volta", modelList("Digər|Vm4|Vm5|Vs1"));
        put("Volvo", modelList("240|340|440|460|740|850|940|960|C30|C70|Digər|EX30|EX90|XC40|XC60|XC70|XC90|S40|S60|S70|S80|S90|V40|V50|V60|V70|V90"));
        put("Voyah", modelList("Digər|Dream|Dreamer|Free|Passion"));
        put("Yadea", modelList("Digər"));
        put("Yamaha", modelList("Aerox|Digər|Fazer|FJR1300|FZ|FZ6|FZ8|XJ6|XMAX|MT-03|MT-07|MT-09|MT-10|NMAX|R1|R3|R6|TMAX|Tracer|YZF-R125"));
        put("ZAZ", modelList("Chance|Digər|Forza|Lanos|Sens|Slavuta|Tavria|Vida"));
        put("Zaza", modelList("Digər"));
        put("ZEEKR", modelList("001|007|009|7X|Digər|X|MIX"));
        put("ZX Auto", modelList("Admiral|Digər|Grand Tiger|Landmark|Territory"));
        put("ZIL", modelList("130|131|4331|5301|Digər"));
        put("Zonsen", modelList("Digər"));
        put("Zontes", modelList("125U|310R|310T|350D|350GK|350X|350T|Digər"));
        put("Wuling", modelList("Air EV|Almaz|Bingo|Cortez|Digər|Hongguang Mini EV|Victory"));
        put("BAIC", modelList("Beijing X3|Beijing X5|Beijing X7|EU5|X35|X55|X7|U5 Plus|Digər"));
        put("Jetour", modelList("Dashing|X50|X70|X70 Plus|X70 Pro|X70S|X90|X90 Plus|X95|T1|T2|Traveller|Digər"));
        put("Zeekr", modelList("001|007|009|7X|X|Digər"));
    }};

    private static final String[] CITIES = {
            "Bakı", "Sumqayıt", "Gəncə", "Xırdalan", "Şəki", "Şamaxı", "Quba", "Qusar", "Xaçmaz", "Lənkəran",
            "Masallı", "Astara", "Mingəçevir", "Yevlax", "Bərdə", "Ağdam", "Ağcabədi", "Qəbələ", "Qax", "Zaqatala",
            "Göyçay", "İsmayıllı", "Qazax", "Tovuz", "Şəmkir", "Salyan", "Şirvan", "Naxçıvan", "Cəlilabad", "Biləsuvar",
            "Ağdaş", "Hacıqabul", "Ağstafa", "Ağsu", "Babək", "Beyləqan", "Balakən", "Goranboy", "Qobustan", "Göygöl",
            "Daşkəsən", "Cəbrayıl", "Culfa", "Zəngilan", "Zərdab", "İmişli", "Gədəbəy", "Kürdəmir", "Laçın", "Lerik",
            "Naftalan", "Neftçala", "Oğuz", "Ordubad", "Saatlı", "Sabirabad", "Samux", "Siyəzən", "Tərtər", "Ucar",
            "Füzuli", "Xankəndi", "Xudat", "Xızı", "Şabran", "Şuşa", "Yardımlı"
    };
    private static final String[] DRIVETRAINS = {"Arxa", "Ön", "Tam"};
    private static final String[] TRANSMISSIONS = {"Avtomat (DHT)", "Avtomat (AT)", "Avtomat (Robot)", "Avtomat (Reduktor)", "Mexaniki (MT)", "Avtomat (Variator)", "Avtomat", "Mexaniki", "Robot", "Variator"};
    private static final String[] COLOR_NAMES = {"Qara", "Yaş Asfalt", "Boz", "Gümüşü", "Ağ", "Bej", "Tünd qırmızı", "Qırmızı", "Çəhrayı", "Narıncı", "Qızılı", "Sarı", "Xaki", "Tünd yaşıl", "Yaşıl", "Açıq yaşıl", "Mavi", "Göy", "Bənövşəyi", "Qəhvəyi"};
    private static final int[] COLOR_VALUES = {Color.BLACK, Color.rgb(70,82,100), Color.GRAY, Color.LTGRAY, Color.WHITE, Color.rgb(245,245,210), Color.rgb(170,45,55), Color.RED, Color.rgb(245,160,180), Color.rgb(255,145,0), Color.rgb(255,190,0), Color.YELLOW, Color.rgb(78,95,40), Color.rgb(34,95,65), Color.rgb(10,190,35), Color.rgb(140,210,160), Color.rgb(55,165,230), Color.BLUE, PURPLE, Color.rgb(170,90,8)};
    private static final String[] CONDITIONS = {"Yeni", "Sürülmüş"};
    private static final String[] SALE_TYPES = {"Satışdadır", "Sifarişlə"};
    private static final String[] SEATS = {"1", "2", "3", "4", "5", "6", "7", "8+"};
    private static final String[] OWNERS = {"1", "2", "3", "4 və daha çox"};
    private static final String[] EQUIPMENT = {"Yüngül lehimli disklər", "ABS", "Lyuk", "Yağış sensoru", "Mərkəzi qapanma", "Park radarı", "Kondisioner", "Oturacaqların isidilməsi", "Dəri salon", "Ksenon lampalar", "360º kamera", "Arxa görüntü kamerası", "Yan pərdələr", "Oturacaqların ventilyasiyası"};
    private static final String[] POPULAR_FILTER_BRANDS = {"Mercedes", "LADA (VAZ)", "Hyundai", "Toyota", "Kia", "BMW", "Chevrolet", "Changan"};
    private static final String[] BODY_TYPES = {"Sedan", "Kupe", "Universal", "Hetçbek", "Liftbek", "Fastbek", "Kabriolet", "Roadster", "Offroader / SUV", "Pikap", "Miniven", "Furqon"};
    private static final String[] FILTER_FUELS = {"Benzin", "Dizel", "Qaz", "Elektro", "Hibrid", "Plug-in Hibrid", "Hidrogen", "Dizel-Hibrid"};
    private static final String[] CURRENCIES = {"AZN", "USD", "EUR"};
    private static final String[] MARKET_TYPES = {"Bazar", "Rəsmi diler", "Avtosalon"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreNativeSession();
        seedPromotionServices();
        CookieHandler.setDefault(nativeCookieManager);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        buildShell();
        initPlayBilling();
        fetchPromotionPlans();
        loadingRemote = true;
        showHome(false);
        fetchListings();
    }

    @Override
    public void onBackPressed() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
            return;
        }
        if ("detail".equals(activeScreen) || "info".equals(activeScreen) || "premium".equals(activeScreen) || "vip".equals(activeScreen) || "promote".equals(activeScreen)
                || "login".equals(activeScreen) || "cabinet".equals(activeScreen) || "add".equals(activeScreen)) {
            showHome(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userLoggedIn && !TextUtils.isEmpty(pendingPromotionOrderId)) {
            checkPromotionStatus(pendingPromotionOrderId, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { if (billingClient != null) billingClient.endConnection(); } catch (Exception ignored) { }
        executor.shutdownNow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_ADD_IMAGES || resultCode != RESULT_OK || data == null) return;
        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            ClipData clip = data.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    Uri uri = clip.getItemAt(i).getUri();
                    if (uri != null) {
                        try { getContentResolver().takePersistableUriPermission(uri, flags); } catch (Exception ignored) { }
                        addDraft.addImage(uri.toString());
                    }
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                try { getContentResolver().takePersistableUriPermission(uri, flags); } catch (Exception ignored) { }
                addDraft.addImage(uri.toString());
            }
            Toast.makeText(this, addDraft.imageUris.size() + " şəkil seçildi", Toast.LENGTH_SHORT).show();
            if ("add".equals(activeScreen)) showAddPlaceholder();
        } catch (Exception e) {
            Toast.makeText(this, "Şəkil seçimi alınmadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        setContentView(root);

        root.addView(header(), new LinearLayout.LayoutParams(-1, dp(72)));

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.setClipToPadding(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(16), dp(14), dp(36));
        scrollView.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomNav = bottomNavigation();
        root.addView(bottomNav, new LinearLayout.LayoutParams(-1, dp(76)));

        detailContactBar = detailStickyBar(null);
        detailContactBar.setVisibility(View.GONE);
        root.addView(detailContactBar, new LinearLayout.LayoutParams(-1, dp(76)));

        installSystemBarInsets();
    }

    private void installSystemBarInsets() {
        if (root == null || Build.VERSION.SDK_INT < 30) return;
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            int top = Math.max(0, bars.top);
            int bottom = Math.max(0, bars.bottom);
            if (top != systemTopInset || bottom != systemBottomInset) {
                systemTopInset = top;
                systemBottomInset = bottom;
                root.setPadding(0, systemTopInset, 0, systemBottomInset);
            }
            return insets;
        });
        root.post(() -> root.requestApplyInsets());
    }

    private View header() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(8), dp(14), dp(8));
        header.setBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 21) header.setElevation(dp(3));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(circle(Color.WHITE, BORDER, 1));
        roundClip(logo, dp(24));
        logo.setOnClickListener(v -> resetSearchAndShowHome());
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        header.addView(logo, logoLp);

        TextView brand = text("BYQEZI.AZ", 16, PURPLE, true);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setLetterSpacing(-0.02f);
        brand.setOnClickListener(v -> resetSearchAndShowHome());
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(0, -1, 1);
        brandLp.leftMargin = dp(10);
        header.addView(brand, brandLp);

        return header;
    }

    private LinearLayout bottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), 0, dp(8), dp(4));
        nav.setBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 21) nav.setElevation(dp(16));

        LinearLayout home = navButton("⌂", "Əsas", () -> showHome(false));
        LinearLayout vip = navButton("♥", "VIP", this::showVip);
        LinearLayout add = navCenterButton(() -> requireLoginFor("add"));
        LinearLayout search = navButton("⌕", "Axtarış", () -> showHome(true));
        LinearLayout cabinet = navButton("●", "Kabinet", () -> requireLoginFor("cabinet"));

        navHomeLabel = (TextView) home.getChildAt(1);
        navVipLabel = (TextView) vip.getChildAt(1);
        navAddLabel = (TextView) add.getChildAt(1);
        navSearchLabel = (TextView) search.getChildAt(1);
        navCabinetLabel = (TextView) cabinet.getChildAt(1);

        nav.addView(home, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(vip, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(add, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(search, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(cabinet, new LinearLayout.LayoutParams(0, -1, 1));
        return nav;
    }

    private LinearLayout navButton(String icon, String label, Runnable click) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setOnClickListener(v -> click.run());
        TextView i = text(icon, 23, Color.rgb(135, 138, 150), true);
        i.setGravity(Gravity.CENTER);
        TextView l = text(label, 11, Color.rgb(112, 113, 126), true);
        l.setGravity(Gravity.CENTER);
        box.addView(i, new LinearLayout.LayoutParams(-1, dp(32)));
        box.addView(l, new LinearLayout.LayoutParams(-1, dp(24)));
        return box;
    }

    private LinearLayout navCenterButton(Runnable click) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setOnClickListener(v -> click.run());
        TextView plus = text("+", 28, Color.WHITE, true);
        plus.setGravity(Gravity.CENTER);
        plus.setBackground(circle(PURPLE, Color.WHITE, 4));
        if (Build.VERSION.SDK_INT >= 21) plus.setElevation(dp(8));
        TextView l = text("Elan", 11, PURPLE, true);
        l.setGravity(Gravity.CENTER);
        box.addView(plus, new LinearLayout.LayoutParams(dp(50), dp(50)));
        box.addView(l, new LinearLayout.LayoutParams(-1, dp(20)));
        return box;
    }

    private void updateNav() {
        int off = Color.rgb(112, 113, 126);
        if (navHomeLabel != null) navHomeLabel.setTextColor("home".equals(activeScreen) ? PURPLE : off);
        if (navVipLabel != null) navVipLabel.setTextColor("vip".equals(activeScreen) ? PURPLE : off);
        if (navAddLabel != null) navAddLabel.setTextColor("add".equals(activeScreen) ? PURPLE_DARK : PURPLE);
        if (navSearchLabel != null) navSearchLabel.setTextColor("search".equals(activeScreen) ? PURPLE : off);
        if (navCabinetLabel != null) navCabinetLabel.setTextColor("cabinet".equals(activeScreen) || "login".equals(activeScreen) ? PURPLE : off);
    }

    private void setDetailMode(boolean detail, Listing listing) {
        if (bottomNav != null) bottomNav.setVisibility(detail ? View.GONE : View.VISIBLE);
        if (detailContactBar != null) {
            detailContactBar.setVisibility(detail ? View.VISIBLE : View.GONE);
            if (detail) {
                root.removeView(detailContactBar);
                detailContactBar = detailStickyBar(listing);
                root.addView(detailContactBar, new LinearLayout.LayoutParams(-1, dp(76)));
            }
        }
    }

    private void showHome(boolean focusSearch) {
        activeScreen = focusSearch ? "search" : "home";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(14), dp(16), dp(14), dp(36));
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        content.addView(searchCard(focusSearch), topLp(0));
        // Production native app: no demo/local notice is shown.
        ImageView ad = localImage(R.drawable.home_ad_03);
        LinearLayout.LayoutParams adLp = new LinearLayout.LayoutParams(-1, dp(203));
        adLp.topMargin = dp(14);
        content.addView(ad, adLp);

        ArrayList<Listing> visible = visibleRows(false);
        ArrayList<Listing> regularRows = regularRows();

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(0, dp(26), 0, dp(2));
        heading.addView(text("Avtomobil almaq", 27, TEXT, true));
        TextView count = text(regularRows.size() + " elan", 14, MUTED, false);
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(-1, -2);
        countLp.topMargin = dp(8);
        heading.addView(count, countLp);
        content.addView(heading);

        // Round 7: Do not render a separate preview grid under "Avtomobil almaq".
        // The two cards here were duplicates of normal listings and visually looked like a broken
        // extra block above PREMİUM ELANLAR. Regular listings stay only in BÜTÜN ELANLAR below.
        ArrayList<Listing> premiumRows = premiumRows();
        if (!premiumRows.isEmpty()) {
            content.addView(sectionHeader("PREMİUM ELANLAR", "Bütün Premium elanlar", this::showPremium), topLp(26));
            addGrid(content, premiumRows, premiumRows.size(), dp(10));
        }

        ArrayList<Listing> vipRows = vipRows();
        if (!vipRows.isEmpty()) {
            content.addView(sectionHeader("VIP ELANLAR", "Bütün VIP elanlar", this::showVip), topLp(26));
            addGrid(content, vipRows, vipRows.size(), dp(10));
        }

        content.addView(sectionHeader("BÜTÜN ELANLAR", visible.size() + " nəticə", null), topLp(30));
        addGrid(content, visible, visible.size(), dp(10));
        footer();

        if (focusSearch) {
            main.postDelayed(() -> {
                if (currentBrandInput != null) {
                    currentBrandInput.requestFocus();
                    currentBrandInput.showDropDown();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(currentBrandInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 250);
        }
    }

    private void showVip() {
        activeScreen = "vip";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(14), dp(16), dp(14), dp(36));
        scrollView.post(() -> scrollView.scrollTo(0, 0));
        content.addView(searchCard(false), topLp(0));
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(0, dp(22), 0, dp(6));
        heading.addView(text("VIP elanlar", 26, TEXT, true));
        heading.addView(text("Yalnız aktiv VIP elanlar", 14, MUTED, false));
        content.addView(heading);
        ArrayList<Listing> vipRows = vipRows();
        addGrid(content, vipRows, vipRows.size(), dp(10));
        footer();
    }

    private void showPremium() {
        activeScreen = "premium";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(14), dp(16), dp(14), dp(36));
        scrollView.post(() -> scrollView.scrollTo(0, 0));
        content.addView(searchCard(false), topLp(0));
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(0, dp(22), 0, dp(6));
        heading.addView(text("Premium elanlar", 26, TEXT, true));
        heading.addView(text("Bütün aktiv Premium elanlar", 14, MUTED, false));
        content.addView(heading);
        ArrayList<Listing> premiumRows = premiumRows();
        addGrid(content, premiumRows, premiumRows.size(), dp(10));
        footer();
    }

    private View searchCard(boolean focusSearch) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(round(CARD, BORDER, dp(25), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(1));

        card.addView(label("Marka"));
        currentBrandInput = autoComplete("Marka yazın və ya seçin", filteredBrands(brandQuery));
        currentBrandInput.setText(brandQuery, false);
        card.addView(currentBrandInput, inputLp());

        TextView ml = label("Model");
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-1, -2);
        mlp.topMargin = dp(12);
        card.addView(ml, mlp);

        String initialExactBrand = exactBrand(brandQuery);
        currentModelInput = autoComplete("Əvvəl marka seçin", modelsFor(initialExactBrand));
        currentModelInput.setText(modelQuery, false);
        setModelEnabled(!TextUtils.isEmpty(initialExactBrand), initialExactBrand);
        card.addView(currentModelInput, inputLp());

        currentBrandInput.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) maybeShowBrandDropdown(); });
        currentBrandInput.setOnClickListener(v -> maybeShowBrandDropdown());
        currentBrandInput.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Object raw = parent.getItemAtPosition(position);
                applyBrandSelection(String.valueOf(raw));
            } catch (Exception ignored) {
                // Adapter/popup callbacks can be stale on some Android keyboards/devices.
                // Never let brand selection close the app.
            }
        });
        currentBrandInput.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (suppressBrandWatcher) return;
                brandQuery = s.toString().trim();
                String exact = exactBrand(brandQuery);
                if (!TextUtils.isEmpty(exact)) {
                    brandQuery = exact;
                    modelQuery = "";
                    if (currentModelInput != null) currentModelInput.setText("", false);
                    setModelEnabled(true, exact);
                    return;
                }
                setAdapter(currentBrandInput, filteredBrands(brandQuery));
                if (currentBrandInput.hasFocus() && !TextUtils.isEmpty(brandQuery) && filteredBrands(brandQuery).length > 0) currentBrandInput.showDropDown();
                else currentBrandInput.dismissDropDown();
                modelQuery = "";
                currentModelInput.setText("", false);
                setModelEnabled(false, "");
            }
        });
        currentModelInput.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) { modelQuery = s.toString().trim(); }
        });
        currentModelInput.setOnClickListener(v -> {
            String exact = exactBrand(brandQuery);
            if (TextUtils.isEmpty(exact)) {
                setModelEnabled(false, "");
                return;
            }
            setModelEnabled(true, exact);
            currentModelInput.showDropDown();
        });
        currentModelInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            String exact = exactBrand(brandQuery);
            if (!TextUtils.isEmpty(exact)) {
                setModelEnabled(true, exact);
                currentModelInput.showDropDown();
            }
        });

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(18), 0, 0);
        TextView filters = button(hasAdvancedFilters() ? "Filtrlər •" : "Filtrlər", Color.WHITE, PURPLE, BORDER);
        filters.setOnClickListener(v -> showFiltersDialog());
        TextView reset = button("Sıfırla", Color.WHITE, PURPLE, BORDER);
        reset.setOnClickListener(v -> {
            resetAllFilters();
            hideKeyboard();
            showHome(false);
        });
        TextView search = button("Axtarın", PURPLE, Color.WHITE, PURPLE);
        search.setOnClickListener(v -> {
            hideKeyboard();
            showHome(false);
        });
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(0, dp(58), 1);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, dp(58), 1);
        rlp.leftMargin = dp(8);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, dp(58), 1);
        slp.leftMargin = dp(8);
        actions.addView(filters, flp);
        actions.addView(reset, rlp);
        actions.addView(search, slp);
        card.addView(actions);
        if (hasAdvancedFilters()) {
            TextView active = text("Aktiv filtrlər: " + activeFilterLabel(), 12, PURPLE, true);
            active.setLineSpacing(dp(2), 1.05f);
            active.setPadding(dp(12), dp(10), dp(12), dp(10));
            active.setBackground(round(Color.rgb(249, 243, 255), Color.rgb(224, 204, 245), dp(14), 1));
            card.addView(active, topLp(dp(12)));
        }
        return card;
    }

    private void resetSearchAndShowHome() {
        resetAllFilters();
        hideKeyboard();
        showHome(false);
    }

    private boolean hasAdvancedFilters() {
        return !TextUtils.isEmpty(brandQuery)
                || !TextUtils.isEmpty(modelQuery)
                || !TextUtils.isEmpty(filterCity)
                || !TextUtils.isEmpty(filterMinPrice)
                || !TextUtils.isEmpty(filterMaxPrice)
                || !TextUtils.isEmpty(filterMinYear)
                || !TextUtils.isEmpty(filterMaxYear)
                || !TextUtils.isEmpty(filterCondition)
                || !TextUtils.isEmpty(filterBodyType)
                || !TextUtils.isEmpty(filterMinMileage)
                || !TextUtils.isEmpty(filterMaxMileage)
                || !TextUtils.isEmpty(filterMinEngine)
                || !TextUtils.isEmpty(filterMaxEngine)
                || !TextUtils.isEmpty(filterMinPower)
                || !TextUtils.isEmpty(filterMaxPower)
                || !TextUtils.isEmpty(filterColor)
                || !TextUtils.isEmpty(filterFuel)
                || !TextUtils.isEmpty(filterDrivetrain)
                || !TextUtils.isEmpty(filterTransmission)
                || !TextUtils.isEmpty(filterSeats)
                || !TextUtils.isEmpty(filterOwners)
                || !TextUtils.isEmpty(filterMarket)
                || !TextUtils.isEmpty(filterSaleType)
                || !filterEquipment.isEmpty()
                || !filterStates.isEmpty();
    }

    private void resetAllFilters() {
        brandQuery = "";
        modelQuery = "";
        filterCity = "";
        filterMinPrice = "";
        filterMaxPrice = "";
        filterMinYear = "";
        filterMaxYear = "";
        filterCondition = "";
        filterBodyType = "";
        filterCurrency = "AZN";
        filterMinMileage = "";
        filterMaxMileage = "";
        filterMinEngine = "";
        filterMaxEngine = "";
        filterMinPower = "";
        filterMaxPower = "";
        filterColor = "";
        filterFuel = "";
        filterDrivetrain = "";
        filterTransmission = "";
        filterSeats = "";
        filterOwners = "";
        filterMarket = "";
        filterSaleType = "";
        filterEquipment.clear();
        filterStates.clear();
    }

    private String activeFilterLabel() {
        ArrayList<String> parts = new ArrayList<>();
        if (!TextUtils.isEmpty(brandQuery)) parts.add(brandQuery);
        if (!TextUtils.isEmpty(modelQuery)) parts.add(modelQuery);
        if (!TextUtils.isEmpty(filterCity)) parts.add(filterCity);
        if (!TextUtils.isEmpty(filterCondition)) parts.add(filterCondition);
        if (!TextUtils.isEmpty(filterBodyType)) parts.add(filterBodyType);
        if (!TextUtils.isEmpty(filterMinPrice) || !TextUtils.isEmpty(filterMaxPrice)) {
            parts.add((TextUtils.isEmpty(filterMinPrice) ? "0" : filterMinPrice) + "–" + (TextUtils.isEmpty(filterMaxPrice) ? "∞" : filterMaxPrice) + " " + filterCurrency);
        }
        if (!TextUtils.isEmpty(filterMinYear) || !TextUtils.isEmpty(filterMaxYear)) {
            parts.add((TextUtils.isEmpty(filterMinYear) ? "—" : filterMinYear) + "–" + (TextUtils.isEmpty(filterMaxYear) ? "—" : filterMaxYear) + " il");
        }
        if (!TextUtils.isEmpty(filterFuel)) parts.add(filterFuel);
        if (!TextUtils.isEmpty(filterTransmission)) parts.add(filterTransmission);
        if (!TextUtils.isEmpty(filterColor)) parts.add(filterColor);
        if (!TextUtils.isEmpty(filterMarket)) parts.add(filterMarket);
        if (!TextUtils.isEmpty(filterSaleType)) parts.add(filterSaleType);
        if (!filterEquipment.isEmpty()) parts.add("təchizat: " + filterEquipment.size());
        if (!filterStates.isEmpty()) parts.add("seçim: " + filterStates.size());
        return parts.isEmpty() ? "yoxdur" : TextUtils.join(" • ", parts);
    }

    private void showFiltersDialog() {
        FrameLayout overlay = modalOverlay();
        LinearLayout panel = modalPanel();
        panel.setPadding(0, 0, 0, 0);
        panel.setBackground(round(Color.rgb(248, 247, 251), BORDER, dp(26), 1));

        final String[] selectedCity = {filterCity};
        final String[] selectedBrand = {brandQuery};
        final String[] selectedModel = {modelQuery};
        final String[] selectedCondition = {filterCondition};
        final String[] selectedBody = {filterBodyType};
        final String[] selectedCurrency = {TextUtils.isEmpty(filterCurrency) ? "AZN" : filterCurrency};
        final String[] selectedColor = {filterColor};
        final String[] selectedFuel = {filterFuel};
        final String[] selectedDrive = {filterDrivetrain};
        final String[] selectedTransmission = {filterTransmission};
        final String[] selectedSeats = {filterSeats};
        final String[] selectedOwners = {filterOwners};
        final String[] selectedMarket = {filterMarket};
        final String[] selectedSaleType = {filterSaleType};
        final String[] selectedMinYear = {filterMinYear};
        final String[] selectedMaxYear = {filterMaxYear};
        final ArrayList<String> selectedEquipment = new ArrayList<>(filterEquipment);
        final ArrayList<String> selectedStates = new ArrayList<>(filterStates);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int panelHeight = Math.max(dp(520), screenHeight - dp(28));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(14));
        header.setBackgroundColor(Color.WHITE);

        TextView close = text("✕", 19, PURPLE, true);
        close.setPadding(dp(8), dp(8), dp(8), dp(8));
        close.setOnClickListener(v -> dismissActiveDialog());

        TextView title = text("Filtrlər", 22, TEXT, true);
        title.setGravity(Gravity.CENTER);

        TextView reset = text("Sıfırlayın", 16, PURPLE, true);
        reset.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        reset.setPadding(dp(8), dp(8), dp(8), dp(8));

        header.addView(close, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(reset, new LinearLayout.LayoutParams(0, -2, 1));
        panel.addView(header);

        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        panel.addView(divider, new LinearLayout.LayoutParams(-1, 1));

        ScrollView bodyScroll = new ScrollView(this);
        bodyScroll.setFillViewport(true);
        bodyScroll.setVerticalScrollBarEnabled(false);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(18));
        bodyScroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        panel.addView(bodyScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        String[] cityOptions = withLeadingOption("Bütün Azərbaycan", CITIES);
        TextView cityValue = dialogSelectField(emptyToHint(selectedCity[0], "Bütün Azərbaycan"), TextUtils.isEmpty(selectedCity[0]));
        cityValue.setOnClickListener(v -> showOptionSheet("Şəhər", cityOptions, emptyToHint(selectedCity[0], "Bütün Azərbaycan"), value -> {
            selectedCity[0] = "Bütün Azərbaycan".equals(value) ? "" : value;
            setDialogSelectText(cityValue, emptyToHint(selectedCity[0], "Bütün Azərbaycan"), TextUtils.isEmpty(selectedCity[0]));
        }));
        LinearLayout cityCard = filterDialogCard();
        cityCard.addView(filterCardTitle("Şəhər"));
        cityCard.addView(cityValue, topLp(dp(12)));
        body.addView(cityCard);

        TextView brandValue = dialogSelectField(emptyToHint(selectedBrand[0], "Marka seçin və ya yazın"), TextUtils.isEmpty(selectedBrand[0]));
        TextView modelValue = dialogSelectField(emptyToHint(selectedModel[0], TextUtils.isEmpty(selectedBrand[0]) ? "Əvvəl marka seçin" : "Model seçin"), TextUtils.isEmpty(selectedModel[0]));
        brandValue.setOnClickListener(v -> showOptionSheet("Marka", BRANDS, selectedBrand[0], value -> {
            selectedBrand[0] = value;
            selectedModel[0] = "";
            setDialogSelectText(brandValue, emptyToHint(selectedBrand[0], "Marka seçin və ya yazın"), TextUtils.isEmpty(selectedBrand[0]));
            setDialogSelectText(modelValue, emptyToHint(selectedModel[0], "Model seçin"), TextUtils.isEmpty(selectedModel[0]));
        }));
        modelValue.setOnClickListener(v -> {
            String exact = exactBrand(selectedBrand[0]);
            if (TextUtils.isEmpty(exact)) {
                Toast.makeText(this, "Əvvəl marka seçin", Toast.LENGTH_SHORT).show();
                return;
            }
            showOptionSheet("Model", modelsFor(exact), selectedModel[0], value -> {
                selectedModel[0] = value;
                setDialogSelectText(modelValue, emptyToHint(selectedModel[0], "Model seçin"), TextUtils.isEmpty(selectedModel[0]));
            });
        });
        LinearLayout brandCard = filterDialogCard();
        brandCard.addView(filterCardTitle("Marka və model"));

        brandCard.addView(filterTwoCol(addField("Marka", brandValue), addField("Model", modelValue)), topLp(dp(14)));
        body.addView(brandCard, topLp(dp(12)));

        LinearLayout basicCard = filterDialogCard();
        basicCard.addView(filterCardTitle("Əsas seçimlər"));
        basicCard.addView(filterSubTitle("Vəziyyət"), topLp(dp(14)));
        LinearLayout conditionRow = new LinearLayout(this);
        conditionRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] conditionLabels = {"Hamısı", "Yeni", "Sürülmüş"};
        TextView[] conditionChips = new TextView[conditionLabels.length];
        for (int i = 0; i < conditionLabels.length; i++) {
            final int index = i;
            boolean isSelected = (TextUtils.isEmpty(selectedCondition[0]) && i == 0) || conditionLabels[i].equalsIgnoreCase(selectedCondition[0]);
            TextView chip = dialogChoiceChip(conditionLabels[i], isSelected);
            chip.setOnClickListener(v -> {
                selectedCondition[0] = index == 0 ? "" : conditionLabels[index];
                for (int j = 0; j < conditionChips.length; j++) {
                    styleDialogChoiceChip(conditionChips[j], j == index);
                }
            });
            conditionChips[i] = chip;
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(54), 1);
            if (i > 0) clp.leftMargin = dp(8);
            conditionRow.addView(chip, clp);
        }
        basicCard.addView(conditionRow, topLp(dp(10)));

        TextView bodyTypeValue = dialogSelectField(emptyToHint(selectedBody[0], "Ban növünü seçin"), TextUtils.isEmpty(selectedBody[0]));
        bodyTypeValue.setOnClickListener(v -> showOptionSheet("Ban növü", BODY_TYPES, selectedBody[0], value -> {
            selectedBody[0] = value;
            setDialogSelectText(bodyTypeValue, value, false);
        }));
        basicCard.addView(addField("Ban növü", bodyTypeValue), topLp(dp(16)));

        basicCard.addView(filterSubTitle("Qiymət və il"), topLp(dp(16)));
        EditText minPrice = filterInput("Qiymət min.", filterMinPrice, InputType.TYPE_CLASS_NUMBER);
        EditText maxPrice = filterInput("Qiymət maks.", filterMaxPrice, InputType.TYPE_CLASS_NUMBER);
        basicCard.addView(filterTwoCol(minPrice, maxPrice), topLp(dp(10)));

        TextView currencyValue = dialogSelectField(selectedCurrency[0], false);
        currencyValue.setOnClickListener(v -> showOptionSheet("Valyuta", CURRENCIES, selectedCurrency[0], value -> {
            selectedCurrency[0] = value;
            setDialogSelectText(currencyValue, value, false);
        }));
        TextView minYearValue = dialogSelectField(emptyToHint(filterMinYear, "İl min."), TextUtils.isEmpty(filterMinYear));
        minYearValue.setOnClickListener(v -> showOptionSheet("İl min.", YEAR_OPTIONS, selectedMinYear[0], value -> { selectedMinYear[0] = value; setDialogSelectText(minYearValue, value, false); }));
        TextView maxYearValue = dialogSelectField(emptyToHint(filterMaxYear, "İl maks."), TextUtils.isEmpty(filterMaxYear));
        maxYearValue.setOnClickListener(v -> showOptionSheet("İl maks.", YEAR_OPTIONS, selectedMaxYear[0], value -> { selectedMaxYear[0] = value; setDialogSelectText(maxYearValue, value, false); }));
        basicCard.addView(filterTwoCol(currencyValue, minYearValue), topLp(dp(8)));
        basicCard.addView(filterSingleRow(maxYearValue), topLp(dp(8)));
        body.addView(basicCard, topLp(dp(12)));

        LinearLayout techCard = filterDialogCard();
        techCard.addView(filterCardTitle("Texniki göstəricilər"));
        techCard.addView(filterSubTitle("Yürüş, mühərrik və güc"), topLp(dp(14)));
        EditText minMileage = filterInput("Yürüş min.", filterMinMileage, InputType.TYPE_CLASS_NUMBER);
        EditText maxMileage = filterInput("Yürüş maks.", filterMaxMileage, InputType.TYPE_CLASS_NUMBER);
        EditText minEngine = filterInput("Mühərrik min.", filterMinEngine, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText maxEngine = filterInput("Mühərrik maks.", filterMaxEngine, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText minPower = filterInput("Güc min.", filterMinPower, InputType.TYPE_CLASS_NUMBER);
        EditText maxPower = filterInput("Güc maks.", filterMaxPower, InputType.TYPE_CLASS_NUMBER);
        techCard.addView(filterTwoCol(minMileage, maxMileage), topLp(dp(10)));
        techCard.addView(filterTwoCol(minEngine, maxEngine), topLp(dp(8)));
        techCard.addView(filterTwoCol(minPower, maxPower), topLp(dp(8)));

        TextView colorValue = dialogSelectField(emptyToHint(selectedColor[0], "Rəng"), TextUtils.isEmpty(selectedColor[0]));
        colorValue.setOnClickListener(v -> showOptionSheet("Rəng", COLOR_NAMES, selectedColor[0], value -> {
            selectedColor[0] = value;
            setDialogSelectText(colorValue, value, false);
        }));
        techCard.addView(addField("Rəng", colorValue), topLp(dp(16)));
        techCard.addView(filterSubTitle("Yanacaq"), topLp(dp(16)));
        techCard.addView(buildSingleChoiceWrap(FILTER_FUELS, 2, selectedFuel), topLp(dp(10)));
        techCard.addView(filterSubTitle("Ötürücü"), topLp(dp(16)));
        techCard.addView(buildSingleChoiceWrap(DRIVETRAINS, 3, selectedDrive), topLp(dp(10)));
        techCard.addView(filterSubTitle("Sürətlər qutusu"), topLp(dp(16)));
        techCard.addView(buildSingleChoiceWrap(TRANSMISSIONS, 2, selectedTransmission), topLp(dp(10)));
        techCard.addView(filterSubTitle("Yerlərin sayı"), topLp(dp(16)));
        techCard.addView(buildSingleChoiceWrap(SEATS, 4, selectedSeats), topLp(dp(10)));
        body.addView(techCard, topLp(dp(12)));

        LinearLayout extraCard = filterDialogCard();
        extraCard.addView(filterCardTitle("Əlavə filtrlər"));
        extraCard.addView(filterSubTitle("Avtomobilin təchizatı"), topLp(dp(14)));
        extraCard.addView(buildMultiChoiceWrap(EQUIPMENT, 1, selectedEquipment), topLp(dp(10)));
        extraCard.addView(filterSubTitle("Vəziyyəti"), topLp(dp(16)));
        extraCard.addView(buildMultiChoiceWrap(new String[]{"Vuruğu yoxdur", "Rənglənməyib", "Yalnız qəzalı avtomobillər", "Kredit", "Barter", "Yalnız şəkilli"}, 1, selectedStates), topLp(dp(10)));
        body.addView(extraCard, topLp(dp(12)));

        LinearLayout saleCard = filterDialogCard();
        saleCard.addView(filterCardTitle("Sahiblər və bazar"));
        TextView ownersValue = dialogSelectField(emptyToHint(selectedOwners[0], "Sahiblər"), TextUtils.isEmpty(selectedOwners[0]));
        ownersValue.setOnClickListener(v -> showOptionSheet("Sahiblər", OWNERS, selectedOwners[0], value -> {
            selectedOwners[0] = value;
            setDialogSelectText(ownersValue, value, false);
        }));
        TextView marketValue = dialogSelectField(emptyToHint(selectedMarket[0], "Bazar"), TextUtils.isEmpty(selectedMarket[0]));
        marketValue.setOnClickListener(v -> showOptionSheet("Bazar", MARKET_TYPES, selectedMarket[0], value -> {
            selectedMarket[0] = value;
            setDialogSelectText(marketValue, value, false);
        }));
        saleCard.addView(filterTwoCol(addField("Sahiblər", ownersValue), addField("Bazar", marketValue)), topLp(dp(12)));
        TextView saleTypeValue = dialogSelectField(emptyToHint(selectedSaleType[0], "Satış tipi"), TextUtils.isEmpty(selectedSaleType[0]));
        saleTypeValue.setOnClickListener(v -> showOptionSheet("Satış tipi", SALE_TYPES, selectedSaleType[0], value -> {
            selectedSaleType[0] = value;
            setDialogSelectText(saleTypeValue, value, false);
        }));
        saleCard.addView(addField("Satış tipi", saleTypeValue), topLp(dp(12)));
        body.addView(saleCard, topLp(dp(12)));

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(dp(14), dp(12), dp(14), dp(14));
        footer.setBackgroundColor(Color.WHITE);
        View footerDivider = new View(this);
        footerDivider.setBackgroundColor(BORDER);
        panel.addView(footerDivider, new LinearLayout.LayoutParams(-1, 1));
        TextView apply = button("Axtarın", PURPLE, Color.WHITE, PURPLE);
        apply.setMinHeight(dp(60));
        apply.setOnClickListener(v -> {
            filterCity = selectedCity[0] == null ? "" : selectedCity[0].trim();
            filterMinPrice = digitsOnly(minPrice.getText() == null ? "" : minPrice.getText().toString());
            filterMaxPrice = digitsOnly(maxPrice.getText() == null ? "" : maxPrice.getText().toString());
            filterMinYear = cleanFilterYearValue(selectedMinYear[0]);
            filterMaxYear = cleanFilterYearValue(selectedMaxYear[0]);
            filterCondition = selectedCondition[0] == null ? "" : selectedCondition[0].trim();
            filterBodyType = selectedBody[0] == null ? "" : selectedBody[0].trim();
            filterCurrency = selectedCurrency[0] == null ? "AZN" : selectedCurrency[0].trim();
            filterMinMileage = digitsOnly(minMileage.getText() == null ? "" : minMileage.getText().toString());
            filterMaxMileage = digitsOnly(maxMileage.getText() == null ? "" : maxMileage.getText().toString());
            filterMinEngine = decimalOnly(minEngine.getText() == null ? "" : minEngine.getText().toString());
            filterMaxEngine = decimalOnly(maxEngine.getText() == null ? "" : maxEngine.getText().toString());
            filterMinPower = digitsOnly(minPower.getText() == null ? "" : minPower.getText().toString());
            filterMaxPower = digitsOnly(maxPower.getText() == null ? "" : maxPower.getText().toString());
            filterColor = selectedColor[0] == null ? "" : selectedColor[0].trim();
            filterFuel = selectedFuel[0] == null ? "" : selectedFuel[0].trim();
            filterDrivetrain = selectedDrive[0] == null ? "" : selectedDrive[0].trim();
            filterTransmission = selectedTransmission[0] == null ? "" : selectedTransmission[0].trim();
            filterSeats = selectedSeats[0] == null ? "" : selectedSeats[0].trim();
            filterOwners = selectedOwners[0] == null ? "" : selectedOwners[0].trim();
            filterMarket = selectedMarket[0] == null ? "" : selectedMarket[0].trim();
            filterSaleType = selectedSaleType[0] == null ? "" : selectedSaleType[0].trim();
            filterEquipment.clear();
            filterEquipment.addAll(selectedEquipment);
            filterStates.clear();
            filterStates.addAll(selectedStates);
            brandQuery = selectedBrand[0] == null ? "" : selectedBrand[0].trim();
            modelQuery = selectedModel[0] == null ? "" : selectedModel[0].trim();
            dismissActiveDialog();
            hideKeyboard();
            showHome(false);
        });
        footer.addView(apply, new LinearLayout.LayoutParams(-1, dp(60)));
        panel.addView(footer);

        reset.setOnClickListener(v -> {
            resetAllFilters();
            brandQuery = "";
            modelQuery = "";
            dismissActiveDialog();
            hideKeyboard();
            showHome(false);
        });

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, panelHeight, Gravity.CENTER);
        lp.leftMargin = dp(8);
        lp.rightMargin = dp(8);
        overlay.addView(panel, lp);
        presentDialog(overlay);
    }


    private String[] withLeadingOption(String first, String[] rest) {
        String[] values = new String[rest.length + 1];
        values[0] = first;
        System.arraycopy(rest, 0, values, 1, rest.length);
        return values;
    }

    private String cleanFilterYearValue(String raw) {
        String digits = digitsOnly(raw);
        if (TextUtils.isEmpty(digits)) return "";
        if (digits.length() > 4) digits = digits.substring(0, 4);
        return digits;
    }

    private String decimalOnly(String raw) {
        String textValue = String.valueOf(raw == null ? "" : raw).replace(',', '.');
        StringBuilder out = new StringBuilder();
        boolean usedDot = false;
        for (int i = 0; i < textValue.length(); i++) {
            char c = textValue.charAt(i);
            if (c >= '0' && c <= '9') out.append(c);
            else if (c == '.' && !usedDot) {
                out.append('.');
                usedDot = true;
            }
        }
        String result = out.toString();
        if (result.startsWith(".")) result = "0" + result;
        if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private double cleanDouble(String raw) {
        try {
            String textValue = decimalOnly(raw);
            return TextUtils.isEmpty(textValue) ? 0d : Double.parseDouble(textValue);
        } catch (Exception ignored) {
            return 0d;
        }
    }

    private boolean containsNormalized(String source, String query) {
        String a = String.valueOf(source == null ? "" : source).trim().toLowerCase(Locale.ROOT);
        String b = String.valueOf(query == null ? "" : query).trim().toLowerCase(Locale.ROOT);
        return TextUtils.isEmpty(b) || a.contains(b);
    }

    private LinearLayout filterDialogCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(round(Color.WHITE, Color.rgb(232, 226, 239), dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(1));
        return card;
    }

    private TextView filterCardTitle(String value) {
        TextView view = text(value, 18, TEXT, true);
        return view;
    }

    private TextView filterSubTitle(String value) {
        TextView view = text(value, 14, Color.rgb(96, 95, 112), true);
        return view;
    }

    private TextView filterSectionTitle(String value) {
        TextView view = text(value, 15, MUTED, true);
        return view;
    }

    private TextView dialogSelectField(String value, boolean placeholder) {
        TextView view = text(value + "   ⌄", 16, placeholder ? LIGHT_MUTED : TEXT, true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setSingleLine(true);
        view.setEllipsize(android.text.TextUtils.TruncateAt.END);
        view.setMinHeight(dp(56));
        view.setPadding(dp(18), 0, dp(18), 0);
        view.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        view.setTag(Boolean.valueOf(placeholder));
        return view;
    }

    private void setDialogSelectText(TextView view, String value, boolean placeholder) {
        view.setText(value + "   ⌄");
        view.setTextColor(placeholder ? LIGHT_MUTED : TEXT);
        view.setTag(Boolean.valueOf(placeholder));
    }

    private EditText filterInput(String hint, String value, int inputType) {
        EditText input = addInput(hint, inputType, value, null);
        input.setTextSize(16);
        input.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        input.setMinHeight(dp(56));
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setPadding(dp(18), 0, dp(18), 0);
        input.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        return input;
    }

    private LinearLayout filterTwoCol(View left, View right) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, -2, 1);
        rlp.leftMargin = dp(10);
        row.addView(right, rlp);
        return row;
    }

    private View filterSingleRow(View child) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(child, new LinearLayout.LayoutParams(-1, -2));
        return wrap;
    }

    private LinearLayout buildPopularBrandGrid(String[] selectedBrand, TextView brandValue, String[] selectedModel, TextView modelValue) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < POPULAR_FILTER_BRANDS.length; i += 4) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            if (i > 0) wrap.addView(row, topLp(dp(8)));
            else wrap.addView(row);
            for (int j = 0; j < 4; j++) {
                int index = i + j;
                if (index >= POPULAR_FILTER_BRANDS.length) {
                    Space space = new Space(this);
                    LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, dp(100), 1);
                    if (j > 0) slp.leftMargin = dp(8);
                    row.addView(space, slp);
                    continue;
                }
                final String brand = POPULAR_FILTER_BRANDS[index];
                TextView tile = popularBrandTile(brand, brand.equalsIgnoreCase(selectedBrand[0]));
                tile.setOnClickListener(v -> {
                    selectedBrand[0] = brand;
                    selectedModel[0] = "";
                    setDialogSelectText(brandValue, brand, false);
                    setDialogSelectText(modelValue, "Model seçin", true);
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(100), 1);
                if (j > 0) lp.leftMargin = dp(8);
                row.addView(tile, lp);
            }
        }
        return wrap;
    }

    private TextView popularBrandTile(String label, boolean selected) {
        TextView tile = text(label, 11, selected ? PURPLE : TEXT, true);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(8), dp(10), dp(8), dp(10));
        tile.setSingleLine(false);
        tile.setMaxLines(2);
        tile.setLineSpacing(dp(2), 1.0f);
        tile.setBackground(round(selected ? Color.rgb(247, 241, 255) : Color.WHITE, selected ? PURPLE : BORDER, dp(18), 1));
        return tile;
    }

    private TextView dialogChoiceChip(String label, boolean selected) {
        TextView chip = text(label, 14, selected ? Color.WHITE : Color.rgb(96, 98, 113), true);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setPadding(dp(10), 0, dp(10), 0);
        chip.setMinHeight(dp(54));
        chip.setBackground(round(selected ? PURPLE : Color.WHITE, selected ? PURPLE : BORDER, dp(16), 1));
        return chip;
    }

    private void styleDialogChoiceChip(TextView chip, boolean selected) {
        chip.setTextColor(selected ? Color.WHITE : Color.rgb(96, 98, 113));
        chip.setBackground(round(selected ? PURPLE : Color.WHITE, selected ? PURPLE : BORDER, dp(14), 1));
    }

    private LinearLayout buildSingleChoiceWrap(String[] values, int perRow, String[] selectedRef) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        int usedUnits = 0;
        for (int i = 0; i < values.length; i++) {
            final String value = values[i];
            int span = estimatedFilterSpan(value, perRow);
            if (row == null || usedUnits + span > perRow) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (wrap.getChildCount() > 0) wrap.addView(row, topLp(dp(8)));
                else wrap.addView(row);
                usedUnits = 0;
            }
            TextView chip = filterChoiceChip(value, value.equalsIgnoreCase(String.valueOf(selectedRef[0])));
            chip.setOnClickListener(v -> {
                selectedRef[0] = value.equalsIgnoreCase(String.valueOf(selectedRef[0])) ? "" : value;
                refreshSingleChoiceWrap(wrap, selectedRef);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, span);
            if (usedUnits > 0) lp.leftMargin = dp(8);
            row.addView(chip, lp);
            usedUnits += span;
        }
        return wrap;
    }

    private void refreshSingleChoiceWrap(LinearLayout wrap, String[] selectedRef) {
        for (int i = 0; i < wrap.getChildCount(); i++) {
            View rowView = wrap.getChildAt(i);
            if (!(rowView instanceof LinearLayout)) continue;
            LinearLayout row = (LinearLayout) rowView;
            for (int j = 0; j < row.getChildCount(); j++) {
                View child = row.getChildAt(j);
                if (!(child instanceof TextView)) continue;
                TextView chip = (TextView) child;
                Object tag = chip.getTag();
                String label = tag == null ? "" : String.valueOf(tag);
                styleFilterChoiceChip(chip, label.equalsIgnoreCase(String.valueOf(selectedRef[0])));
            }
        }
    }

    private int estimatedFilterSpan(String value, int perRow) {
        int len = value == null ? 0 : value.length();
        if (perRow <= 2) return 1;
        if (perRow == 3) return len > 11 ? 2 : 1;
        if (perRow >= 6) return 1;
        if (len > 14) return 2;
        return 1;
    }

    private TextView filterChoiceChip(String label, boolean selected) {
        TextView chip = text(label, label != null && label.length() > 12 ? 11 : 13, selected ? PURPLE : Color.rgb(96, 98, 113), true);
        chip.setTag(label);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(10), dp(12), dp(10), dp(12));
        chip.setSingleLine(false);
        chip.setMaxLines(2);
        chip.setMinHeight(dp(52));
        chip.setLineSpacing(dp(2), 1.0f);
        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        styleFilterChoiceChip(chip, selected);
        return chip;
    }

    private void styleFilterChoiceChip(TextView chip, boolean selected) {
        chip.setTextColor(selected ? PURPLE : Color.rgb(96, 98, 113));
        chip.setBackground(round(selected ? Color.rgb(247, 241, 255) : Color.WHITE, selected ? Color.rgb(220, 199, 242) : BORDER, dp(16), 1));
    }

    private LinearLayout buildMultiChoiceWrap(String[] values, int perRow, ArrayList<String> selected) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < values.length; i++) {
            if (i % perRow == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (i > 0) wrap.addView(row, topLp(dp(8)));
                else wrap.addView(row);
            }
            final String value = values[i];
            TextView chip = filterMultiChip(value, selected.contains(value));
            chip.setOnClickListener(v -> {
                if (selected.contains(value)) selected.remove(value);
                else selected.add(value);
                styleFilterMultiChip(chip, selected.contains(value));
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            if (i % perRow != 0) lp.leftMargin = dp(8);
            row.addView(chip, lp);
        }
        return wrap;
    }

    private TextView filterMultiChip(String label, boolean selected) {
        TextView chip = text((selected ? "✓  " : "○  ") + label, 13, selected ? PURPLE : TEXT, true);
        chip.setTag(label);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setSingleLine(false);
        chip.setMaxLines(2);
        chip.setMinHeight(dp(60));
        chip.setPadding(dp(14), dp(10), dp(14), dp(10));
        chip.setLineSpacing(dp(2), 1.0f);
        chip.setBackground(round(selected ? Color.rgb(248, 241, 255) : Color.WHITE, selected ? Color.rgb(220, 199, 242) : BORDER, dp(16), 1));
        return chip;
    }

    private void styleFilterMultiChip(TextView chip, boolean selected) {
        String label = String.valueOf(chip.getTag() == null ? "" : chip.getTag());
        chip.setText((selected ? "✓  " : "○  ") + label);
        chip.setTextColor(selected ? PURPLE : TEXT);
        chip.setBackground(round(selected ? Color.rgb(248, 241, 255) : Color.WHITE, selected ? Color.rgb(220, 199, 242) : BORDER, dp(16), 1));
    }

    private void applyBrandSelection(String value) {
        String exact = exactBrand(value);
        if (TextUtils.isEmpty(exact)) {
            brandQuery = value == null ? "" : value.trim();
            modelQuery = "";
            setModelEnabled(false, "");
            if (currentBrandInput != null) currentBrandInput.dismissDropDown();
            return;
        }
        brandQuery = exact;
        modelQuery = "";
        suppressBrandWatcher = true;
        try {
            if (currentBrandInput != null) {
                currentBrandInput.setText(exact, false);
                currentBrandInput.dismissDropDown();
            }
            if (currentModelInput != null) {
                currentModelInput.setText("", false);
                setModelEnabled(true, exact);
            }
        } catch (Exception ignored) {
            // Keep the selected brand in state even if the dropdown view is already detached.
        } finally {
            suppressBrandWatcher = false;
        }
    }

    private void addGrid(LinearLayout parent, ArrayList<Listing> rows, int limit, int topMargin) {
        if (rows.isEmpty()) {
            TextView empty = text("Uyğun elan tapılmadı", 15, MUTED, true);
            empty.setGravity(Gravity.CENTER);
            empty.setBackground(round(Color.WHITE, BORDER, dp(16), 1));
            empty.setPadding(dp(18), dp(24), dp(18), dp(24));
            parent.addView(empty, topLp(dp(8)));
            return;
        }
        int count = Math.min(limit, rows.size());
        for (int i = 0; i < count; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
            rowLp.topMargin = i == 0 ? topMargin : dp(10);
            Listing left = rows.get(i);
            View leftCard = carCard(left);
            row.addView(leftCard, new LinearLayout.LayoutParams(0, -2, 1));
            if (i + 1 < count) {
                Listing right = rows.get(i + 1);
                View rightCard = carCard(right);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, -2, 1);
                rlp.leftMargin = dp(10);
                row.addView(rightCard, rlp);
            } else {
                Space sp = new Space(this);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, 1, 1);
                rlp.leftMargin = dp(10);
                row.addView(sp, rlp);
            }
            parent.addView(row, rowLp);
        }
    }

    private View carCard(Listing listing) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(round(Color.WHITE, BORDER, dp(12), 1));
        card.setClipToOutline(false);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));
        card.setOnClickListener(v -> showDetail(listing));

        FrameLayout media = new FrameLayout(this);
        media.setBackground(round(VIP_BG, Color.TRANSPARENT, dp(12), 0));
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindListingImage(listing, img);
        roundClip(img, dp(12));
        media.addView(img, new FrameLayout.LayoutParams(-1, dp(128)));

        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.setPadding(dp(6), dp(6), dp(6), 0);
        if (listing.isPremiumActive()) badges.addView(tinyBadge("PREMIUM", GOLD, TEXT));
        if (listing.isVipActive()) badges.addView(tinyBadge("VIP", PURPLE, Color.WHITE));
        media.addView(badges, new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT));

        TextView fav = text(isFavorite(listing) ? "♥" : "♡", 20, isFavorite(listing) ? Color.rgb(255, 31, 60) : Color.rgb(62, 58, 74), true);
        fav.setGravity(Gravity.CENTER);
        fav.setBackground(circle(Color.argb(242, 255, 255, 255), BORDER, 1));
        fav.setOnClickListener(v -> toggleFavorite(listing, fav));
        FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.TOP | Gravity.RIGHT);
        favLp.setMargins(0, dp(7), dp(7), 0);
        media.addView(fav, favLp);
        card.addView(media, new LinearLayout.LayoutParams(-1, dp(128)));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(8), dp(8), dp(8), dp(10));
        TextView price = text(formatMoney(listing.price, listing.currency), 15, Color.BLACK, true);
        body.addView(price);
        TextView title = text(listing.title, 12, TEXT, true);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        body.addView(title);
        TextView specs = text(listing.specLine(), 10, MUTED, false);
        specs.setSingleLine(true);
        specs.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams specLp = new LinearLayout.LayoutParams(-1, -2);
        specLp.topMargin = dp(3);
        body.addView(specs, specLp);
        ArrayList<String> metaParts = new ArrayList<>();
        if (!TextUtils.isEmpty(listing.city)) metaParts.add(listing.city);
        if (!TextUtils.isEmpty(listing.time)) metaParts.add(listing.time);
        String metaText = TextUtils.join("  •  ", metaParts);
        if (!TextUtils.isEmpty(metaText)) {
            TextView meta = text(metaText, 10, MUTED, false);
            LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(-1, -2);
            metaLp.topMargin = dp(4);
            body.addView(meta, metaLp);
        }
        card.addView(body, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    private View sectionHeader(String title, String right, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView marker = text("▌", 16, PURPLE, true);
        row.addView(marker, new LinearLayout.LayoutParams(dp(10), -2));
        TextView left = text(title, 19, TEXT, true);
        left.setLetterSpacing(0.02f);
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        if (!TextUtils.isEmpty(right)) {
            TextView r = text(right, 12, PURPLE, true);
            r.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            if (action != null) r.setOnClickListener(v -> action.run());
            row.addView(r, new LinearLayout.LayoutParams(-2, -2));
        }
        return row;
    }

    private View localNotice() {
        TextView n = text(loadingRemote ? "Canlı elanlar yüklənir..." : "Canlı məlumat hazırda yüklənmədi. Bir az sonra yenidən yoxlayın.", 12, Color.rgb(99, 64, 0), false);
        n.setLineSpacing(0, 1.08f);
        n.setPadding(dp(12), dp(10), dp(12), dp(10));
        n.setBackground(round(Color.rgb(255, 250, 232), Color.rgb(251, 190, 54), dp(12), 1));
        return n;
    }

    private void footer() {
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.VERTICAL);
        f.setPadding(0, dp(66), 0, dp(8));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("BYQEZI.AZ", 15, PURPLE, true);
        row.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView p = footerLink("Məxfilik", "Məxfilik", "BYQEZI.AZ istifadəçi məlumatlarını yalnız xidmətin işləməsi, elanların idarə olunması və təhlükəsizlik məqsədləri üçün emal edir.");
        TextView a = footerLink("Haqqımızda", "Haqqımızda", "BYQEZI.AZ avtomobil elanlarının rahat yerləşdirilməsi və axtarışı üçün hazırlanmış platformadır.");
        TextView c = footerLink("Əlaqə", "Əlaqə", "Bizimlə əlaqə: support@byqezi.az və +994 10 515 12 78.");
        row.addView(p);
        row.addView(a);
        row.addView(c);
        f.addView(row);
        TextView copy = text("© 2026 www.byqezi.az. Bütün hüquqlar qorunur.", 11, Color.rgb(164, 160, 174), false);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(16);
        f.addView(copy, cp);
        content.addView(f);
    }

    private TextView footerLink(String label, String title, String body) {
        TextView t = text(label, 12, TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(8), dp(0), dp(8));
        t.setOnClickListener(v -> showInfo(title, body));
        return t;
    }


    private void requireLoginFor(String targetScreen) {
        pendingAfterLogin = TextUtils.isEmpty(targetScreen) ? "cabinet" : targetScreen;
        if (userLoggedIn) {
            if ("add".equals(pendingAfterLogin)) showAddPlaceholder();
            else showCabinet();
            return;
        }
        showLogin(pendingAfterLogin);
    }

    private void showLogin(String targetScreen) {
        pendingAfterLogin = TextUtils.isEmpty(targetScreen) ? "cabinet" : targetScreen;
        activeScreen = "login";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(14), dp(22), dp(14), dp(34));
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        TextView back = text("‹ Ana səhifə", 15, PURPLE, true);
        back.setOnClickListener(v -> showHome(false));
        content.addView(back);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));

        boolean verifying = !TextUtils.isEmpty(otpChallengeId) && !TextUtils.isEmpty(otpPhone);
        TextView title = text(verifying ? "SMS kodunu daxil edin" : "Telefonla giriş", 27, TEXT, true);
        card.addView(title);
        TextView intro = text(verifying
                ? normalizePhoneDisplay(otpPhone) + " nömrəsinə göndərilən 6 rəqəmli kodu yazın."
                : "Elan əlavə etmək və kabinetə daxil olmaq üçün mobil nömrənizi təsdiqləyin.", 14, MUTED, false);
        intro.setLineSpacing(dp(2), 1.08f);
        card.addView(intro, topLp(dp(12)));

        if (verifying) {
            EditText code = new EditText(this);
            code.setTextSize(22);
            code.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            code.setTextColor(TEXT);
            code.setHintTextColor(LIGHT_MUTED);
            code.setHint("123456");
            code.setSingleLine(true);
            code.setInputType(InputType.TYPE_CLASS_NUMBER);
            code.setPadding(dp(18), 0, dp(18), 0);
            code.setGravity(Gravity.CENTER);
            code.setBackground(round(FIELD, BORDER, dp(18), 1));
            card.addView(label("SMS kod"), topLp(dp(18)));
            card.addView(code, inputLp());

            TextView verify = button(authLoading ? "Yoxlanılır..." : "Təsdiqlə və daxil ol", PURPLE, Color.WHITE, PURPLE);
            verify.setEnabled(!authLoading);
            LinearLayout.LayoutParams verifyLp = new LinearLayout.LayoutParams(-1, dp(58));
            verifyLp.topMargin = dp(18);
            card.addView(verify, verifyLp);
            verify.setOnClickListener(v -> {
                String c = digitsOnly(code.getText() == null ? "" : code.getText().toString()).trim();
                if (!c.matches("\\d{6}")) {
                    Toast.makeText(this, "SMS kod 6 rəqəm olmalıdır", Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyOtpCode(c);
            });

            TextView reset = text("Nömrəni dəyiş / yeni kod al", 14, PURPLE, true);
            reset.setGravity(Gravity.CENTER);
            reset.setPadding(dp(8), dp(14), dp(8), dp(6));
            reset.setOnClickListener(v -> {
                otpChallengeId = "";
                otpPhone = "";
                authLoading = false;
                showLogin(pendingAfterLogin);
            });
            card.addView(reset, topLp(dp(8)));
        } else {
            EditText phone = new EditText(this);
            phone.setTextSize(18);
            phone.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            phone.setTextColor(TEXT);
            phone.setHintTextColor(LIGHT_MUTED);
            phone.setHint("0556208480");
            phone.setSingleLine(true);
            phone.setInputType(InputType.TYPE_CLASS_PHONE);
            phone.setPadding(dp(18), 0, dp(18), 0);
            phone.setBackground(round(FIELD, BORDER, dp(18), 1));
            if (!TextUtils.isEmpty(userPhone)) phone.setText(normalizePhoneDisplay(userPhone));
            card.addView(label("Telefon nömrəsi"), topLp(dp(18)));
            card.addView(phone, inputLp());

            TextView login = button(authLoading ? "SMS göndərilir..." : "SMS kod göndər", PURPLE, Color.WHITE, PURPLE);
            login.setEnabled(!authLoading);
            LinearLayout.LayoutParams loginLp = new LinearLayout.LayoutParams(-1, dp(58));
            loginLp.topMargin = dp(18);
            card.addView(login, loginLp);

            login.setOnClickListener(v -> {
                String raw = phone.getText() == null ? "" : phone.getText().toString().trim();
                String normalized = normalizeAzPhone(raw);
                if (TextUtils.isEmpty(normalized)) {
                    Toast.makeText(this, "Telefon nömrəsini düzgün yazın: 0556208480", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestOtpCode(normalized);
            });
        }

        content.addView(card, topLp(dp(18)));
    }

    private void requestOtpCode(String phone) {
        if (authLoading) return;
        authLoading = true;
        otpPhone = phone;
        showLogin(pendingAfterLogin);
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("phone", phone);
                JSONObject res = postJson("/api/auth/otp/request", body);
                String challenge = res.optString("challenge_id", "");
                String normalizedPhone = res.optString("phone", phone);
                if (TextUtils.isEmpty(challenge)) throw new Exception("SMS kod sessiyası alınmadı");
                main.post(() -> {
                    authLoading = false;
                    otpChallengeId = challenge;
                    otpPhone = normalizedPhone;
                    Toast.makeText(this, "SMS kod göndərildi", Toast.LENGTH_SHORT).show();
                    showLogin(pendingAfterLogin);
                });
            } catch (Exception e) {
                main.post(() -> {
                    authLoading = false;
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    showLogin(pendingAfterLogin);
                });
            }
        });
    }

    private void verifyOtpCode(String code) {
        if (authLoading) return;
        authLoading = true;
        showLogin(pendingAfterLogin);
        final String phone = otpPhone;
        final String challenge = otpChallengeId;
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("phone", phone);
                body.put("challenge_id", challenge);
                body.put("code", code);
                JSONObject res = postJson("/api/auth/otp/verify", body);
                JSONObject user = res.optJSONObject("user");
                final String name = user == null ? "BYQEZI istifadəçisi" : user.optString("name", "BYQEZI istifadəçisi");
                final String verifiedPhone = user == null ? phone : user.optString("phone", phone);
                userLoggedIn = true;
                userName = TextUtils.isEmpty(name) ? "BYQEZI istifadəçisi" : name;
                userPhone = normalizeAzPhone(verifiedPhone);
                saveNativeSession();
                ArrayList<Listing> mine = requestUserListings();
                main.post(() -> {
                    authLoading = false;
                    userLoggedIn = true;
                    userName = TextUtils.isEmpty(name) ? "BYQEZI istifadəçisi" : name;
                    userPhone = normalizeAzPhone(verifiedPhone);
                    saveNativeSession();
                    otpChallengeId = "";
                    otpPhone = "";
                    userListings.clear();
                    for (Listing row : mine) { row.mine = true; userListings.add(row); }
                    userListingsLastFetchMs = mine.isEmpty() ? 0L : System.currentTimeMillis();
                    hideKeyboard();
                    Toast.makeText(this, "Kabinetə giriş uğurludur", Toast.LENGTH_SHORT).show();
                    if ("add".equals(pendingAfterLogin)) showAddPlaceholder();
                    else showCabinet();
                });
            } catch (Exception e) {
                main.post(() -> {
                    authLoading = false;
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    showLogin(pendingAfterLogin);
                });
            }
        });
    }

    private void showCabinet() {
        if (!userLoggedIn) {
            showLogin("cabinet");
            return;
        }
        activeScreen = "cabinet";
        updateNav();
        setDetailMode(false, null);
        refreshUserListings(userListingsLastFetchMs == 0L);
        content.removeAllViews();
        content.setPadding(dp(14), dp(14), dp(14), dp(38));
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        content.addView(cabinetHeroCard(), topLp(0));
        content.addView(cabinetSupportCard(), topLp(dp(12)));
        content.addView(cabinetStatsGrid(), topLp(dp(14)));

        TextView notice = text("Elan əlavə edildikdən sonra əvvəl Gözləmədə bölməsinə düşür. Admin təsdiqlədikdən sonra Saytda görünür.", 13, Color.rgb(91, 81, 110), false);
        notice.setLineSpacing(dp(4), 1.06f);
        notice.setPadding(dp(18), dp(14), dp(18), dp(14));
        notice.setBackground(round(Color.rgb(250, 247, 255), Color.rgb(223, 212, 242), dp(18), 1));
        content.addView(notice, topLp(dp(16)));

        ArrayList<Listing> visibleRows = cabinetVisibleRows();
        TextView title = text(cabinetListTitle(), 24, TEXT, true);
        title.setLetterSpacing(-0.015f);
        content.addView(title, topLp(dp(24)));
        if (visibleRows.isEmpty()) {
            LinearLayout empty = new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(18), dp(30), dp(18), dp(30));
            empty.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
            if (Build.VERSION.SDK_INT >= 21) empty.setElevation(dp(2));
            TextView emptyTitle = text(userListingsLoading ? "Kabinet yüklənir..." : cabinetEmptyTitle(), 18, TEXT, true);
            emptyTitle.setGravity(Gravity.CENTER);
            empty.addView(emptyTitle);
            TextView emptySub = text(userListingsLoading ? "Real elanlar serverdən alınır." : cabinetEmptySubtitle(), 13, MUTED, false);
            emptySub.setGravity(Gravity.CENTER);
            empty.addView(emptySub, topLp(dp(10)));
            if (!"favorites".equals(cabinetFilter) && "all".equals(cabinetFilter)) {
                TextView addBtn = button("+ Yeni elan əlavə edin", PURPLE, Color.WHITE, PURPLE);
                addBtn.setOnClickListener(v -> showAddPlaceholder());
                LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(-1, dp(50));
                addLp.topMargin = dp(18);
                empty.addView(addBtn, addLp);
            }
            content.addView(empty, topLp(dp(12)));
        } else {
            for (Listing row : visibleRows) content.addView(cabinetListingCard(row), topLp(dp(12)));
        }
    }


    private LinearLayout cabinetHeroCard() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(16));
        hero.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) hero.setElevation(dp(2));

        LinearLayout userRow = new LinearLayout(this);
        userRow.setOrientation(LinearLayout.HORIZONTAL);
        userRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = text("B", 22, Color.WHITE, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(circle(PURPLE, Color.TRANSPARENT, 0));
        userRow.addView(avatar, new LinearLayout.LayoutParams(dp(62), dp(62)));

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.VERTICAL);
        TextView phone = text(normalizePhoneDisplay(userPhone), 17, TEXT, true);
        phone.setLetterSpacing(0.01f);
        TextView name = text(safe(userName, "BYQEZI istifadəçisi"), 13, MUTED, false);
        TextView status = text("Aktiv kabinet", 11, PURPLE, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(5), dp(10), dp(5));
        status.setBackground(round(Color.rgb(246, 238, 255), Color.rgb(224, 204, 245), dp(14), 1));
        meta.addView(phone);
        meta.addView(name, topLp(dp(8)));
        meta.addView(status, topLp(dp(10)));
        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(0, -2, 1);
        metaLp.leftMargin = dp(14);
        userRow.addView(meta, metaLp);
        hero.addView(userRow);

        TextView add = button("+ Yeni elan əlavə edin", PURPLE, Color.WHITE, PURPLE);
        add.setTextSize(15);
        add.setOnClickListener(v -> showAddPlaceholder());
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(-1, dp(46));
        addLp.topMargin = dp(18);
        hero.addView(add, addLp);
        return hero;
    }


    private LinearLayout cabinetSupportCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Dəstək xidməti", 17, TEXT, true);
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView badge = text("09:00–19:00", 11, Color.rgb(18, 135, 82), true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(round(Color.rgb(230, 250, 240), Color.rgb(190, 235, 212), dp(14), 1));
        head.addView(badge);
        card.addView(head);

        TextView phone = text("+994 10 515 12 78", 18, PURPLE, true);
        TextView hours = text("Həftənin 7 günü xidmətinizdəyik", 13, MUTED, false);
        card.addView(phone, topLp(dp(12)));
        card.addView(hours, topLp(dp(7)));

        TextView logout = button("Çıxış", Color.rgb(255, 247, 249), Color.rgb(185, 42, 62), Color.rgb(255, 194, 205));
        logout.setTextSize(14);
        logout.setOnClickListener(v -> {
            clearNativeSession();
            Toast.makeText(this, "Çıxış edildi", Toast.LENGTH_SHORT).show();
            showHome(false);
        });
        LinearLayout.LayoutParams logoutLp = new LinearLayout.LayoutParams(-1, dp(42));
        logoutLp.topMargin = dp(14);
        card.addView(logout, logoutLp);
        return card;
    }


    private LinearLayout cabinetStatsGrid() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        ArrayList<Listing> rows = userListings;
        int active = 0;
        int pending = 0;
        for (Listing row : rows) {
            if (row.isPendingStatus()) pending++;
            else if (row.isActiveStatus()) active++;
        }
        int favorites = cabinetFavoriteRows().size();
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        r1.addView(cabinetStat("Bütün elanlar", rows.size(), "all"), new LinearLayout.LayoutParams(0, dp(82), 1));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, dp(82), 1);
        p2.leftMargin = dp(10);
        r1.addView(cabinetStat("Saytda", active, "active"), p2);
        wrap.addView(r1);
        LinearLayout r2 = new LinearLayout(this);
        r2.setOrientation(LinearLayout.HORIZONTAL);
        r2.addView(cabinetStat("Gözləmədə", pending, "pending"), new LinearLayout.LayoutParams(0, dp(82), 1));
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(0, dp(82), 1);
        p4.leftMargin = dp(10);
        r2.addView(cabinetStat("Favoritlər", favorites, "favorites"), p4);
        wrap.addView(r2, topLp(dp(10)));
        return wrap;
    }


    private LinearLayout cabinetStat(String label, int count, String filter) {
        boolean selected = filter.equals(cabinetFilter);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(10), dp(10), dp(10), dp(10));
        box.setBackground(round(selected ? Color.rgb(248, 241, 255) : Color.WHITE, selected ? Color.rgb(210, 188, 238) : BORDER, dp(20), selected ? 2 : 1));
        if (Build.VERSION.SDK_INT >= 21) box.setElevation(dp(selected ? 2 : 1));
        TextView n = text(String.valueOf(count), 23, PURPLE, true);
        n.setGravity(Gravity.CENTER);
        TextView l = text(label, 12, selected ? PURPLE : Color.rgb(96, 93, 110), true);
        l.setGravity(Gravity.CENTER);
        box.addView(n);
        box.addView(l, topLp(dp(9)));
        box.setClickable(true);
        box.setOnClickListener(v -> {
            cabinetFilter = filter;
            showCabinet();
        });
        return box;
    }


    private ArrayList<Listing> cabinetVisibleRows() {
        ArrayList<Listing> rows = new ArrayList<>();
        if ("favorites".equals(cabinetFilter)) return cabinetFavoriteRows();
        for (Listing row : userListings) {
            if ("active".equals(cabinetFilter)) {
                if (row.isActiveStatus() && !row.isPendingStatus()) rows.add(row);
            } else if ("pending".equals(cabinetFilter)) {
                if (row.isPendingStatus()) rows.add(row);
            } else {
                rows.add(row);
            }
        }
        return rows;
    }

    private ArrayList<Listing> cabinetFavoriteRows() {
        ArrayList<Listing> rows = new ArrayList<>();
        for (Listing row : listings) if (isFavorite(row)) rows.add(row);
        return rows;
    }

    private String cabinetListTitle() {
        if ("active".equals(cabinetFilter)) return "Saytda olan elanlar";
        if ("pending".equals(cabinetFilter)) return "Gözləmədə olanlar";
        if ("favorites".equals(cabinetFilter)) return "Favoritlər";
        return "Mənim elanlarım";
    }

    private String cabinetEmptyTitle() {
        if ("active".equals(cabinetFilter)) return "Saytda elan yoxdur";
        if ("pending".equals(cabinetFilter)) return "Gözləmədə elan yoxdur";
        if ("favorites".equals(cabinetFilter)) return "Favorit elan yoxdur";
        return "Hələ elanınız yoxdur";
    }

    private String cabinetEmptySubtitle() {
        if ("active".equals(cabinetFilter)) return "Admin təsdiqlədikdən sonra elanlar burada görünəcək.";
        if ("pending".equals(cabinetFilter)) return "Təsdiq gözləyən elanlar burada görünəcək.";
        if ("favorites".equals(cabinetFilter)) return "Bəyəndiyiniz elanları ürək ikonuna basaraq əlavə edin.";
        return "Yeni elan əlavə edin və kabinetdə idarə edin.";
    }

    private String cabinetStatusLabel(Listing listing) {
        if (listing != null && listing.isPendingStatus()) return "Gözləmədə";
        if (listing != null && listing.isRejectedStatus()) return "İmtina";
        return "Saytda";
    }

    private int cabinetStatusBg(Listing listing) {
        if (listing != null && listing.isPendingStatus()) return Color.rgb(255, 247, 225);
        if (listing != null && listing.isRejectedStatus()) return Color.rgb(255, 241, 244);
        return Color.rgb(230, 250, 240);
    }

    private int cabinetStatusText(Listing listing) {
        if (listing != null && listing.isPendingStatus()) return Color.rgb(150, 93, 0);
        if (listing != null && listing.isRejectedStatus()) return Color.rgb(178, 48, 64);
        return Color.rgb(18, 135, 82);
    }

    private int cabinetStatusBorder(Listing listing) {
        if (listing != null && listing.isPendingStatus()) return Color.rgb(245, 215, 160);
        if (listing != null && listing.isRejectedStatus()) return Color.rgb(255, 200, 210);
        return Color.rgb(190, 235, 212);
    }


    private LinearLayout cabinetListingCard(Listing listing) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindListingImage(listing, img);
        roundClip(img, dp(16));
        top.addView(img, new LinearLayout.LayoutParams(dp(124), dp(92)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, 0, 0);
        TextView price = text(formatMoney(listing.price, listing.currency), 20, TEXT, true);
        price.setLetterSpacing(-0.015f);
        TextView title = text(safe(listing.title, listing.brand + " " + listing.model), 14, TEXT, true);
        title.setMaxLines(2);
        TextView specs = text(listing.specLine(), 12, MUTED, false);
        info.addView(price);
        info.addView(title, topLp(dp(7)));
        info.addView(specs, topLp(dp(7)));

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.addView(detailChip(cabinetStatusLabel(listing), cabinetStatusBg(listing), cabinetStatusText(listing), cabinetStatusBorder(listing)));
        if (listing.isPremiumActive()) chips.addView(detailChip("Premium", Color.rgb(255, 245, 210), Color.rgb(135, 92, 0), Color.rgb(250, 220, 135)));
        if (listing.isVipActive()) chips.addView(detailChip("VIP", Color.rgb(242, 232, 255), PURPLE, Color.rgb(218, 200, 242)));
        info.addView(chips, topLp(dp(10)));
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(top);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        TextView view = button("Bax", Color.WHITE, PURPLE, BORDER);
        view.setTextSize(14);
        view.setOnClickListener(v -> showDetail(listing));
        TextView promo = button("Reklam", Color.WHITE, PURPLE, BORDER);
        promo.setTextSize(14);
        promo.setOnClickListener(v -> showPromotionServices(listing));
        actions.addView(view, new LinearLayout.LayoutParams(0, dp(42), 1));
        LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(0, dp(42), 1);
        pr.leftMargin = dp(10);
        actions.addView(promo, pr);
        card.addView(actions, topLp(dp(14)));
        return card;
    }


    private void showAddPlaceholder() {
        if (!userLoggedIn) {
            showLogin("add");
            return;
        }
        ensureAddDraftContactDefaults();
        boolean preserveScroll = "add".equals(activeScreen);
        final int restoreScrollY = preserveScroll ? scrollView.getScrollY() : 0;
        activeScreen = "add";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(10), dp(14), dp(10), dp(28));

        TextView back = text("‹ Kabinet", 15, PURPLE, true);
        back.setPadding(0, dp(4), 0, dp(4));
        back.setOnClickListener(v -> showCabinet());
        content.addView(back);

        LinearLayout hero = addPlainCard();
        hero.addView(addProgressStrip());
        content.addView(hero, topLp(dp(10)));

        content.addView(addVehicleTypeSection(), topLp(dp(14)));
        content.addView(addTechnicalSection(), topLp(dp(14)));
        content.addView(addPhotoSection(), topLp(dp(14)));
        content.addView(addPriceConditionSection(), topLp(dp(14)));
        content.addView(addContactSection(), topLp(dp(14)));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(16), dp(16), dp(16), dp(18));
        bottom.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) bottom.setElevation(dp(2));
        TextView hint = text("Məlumatları yoxlayın. Təsdiqdən sonra elan kabinetdə Gözləmədə kimi görünəcək.", 12, MUTED, false);
        hint.setLineSpacing(dp(2), 1.05f);
        bottom.addView(hint);
        TextView submit = button(addSubmitting ? "Göndərilir..." : "Davam edin və təsdiqləyin →", PURPLE, Color.WHITE, PURPLE);
        submit.setTextSize(17);
        submit.setMinHeight(dp(52));
        submit.setPadding(dp(12), dp(12), dp(12), dp(12));
        // Old placeholder text removed from UI: Submit növbəti mərhələdə qoşulacaq
        submit.setEnabled(!addSubmitting);
        submit.setOnClickListener(v -> submitAddListing());
        LinearLayout.LayoutParams submitLp = topLp(dp(14));
        submitLp.height = dp(52);
        bottom.addView(submit, submitLp);
        content.addView(bottom, topLp(dp(14)));
        scrollView.post(() -> scrollView.scrollTo(0, restoreScrollY));
    }

    private void ensureAddDraftContactDefaults() {
        if (TextUtils.isEmpty(addDraft.sellerName)) addDraft.sellerName = TextUtils.isEmpty(userName) ? "BYQEZI istifadəçisi" : userName;
        if (TextUtils.isEmpty(addDraft.phone)) addDraft.phone = TextUtils.isEmpty(userPhone) ? "0556208480" : userPhone;
    }

    private LinearLayout addProgressStrip() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] steps = {"Növ", "Məlumat", "Şəkil", "Qiymət", "Əlaqə"};
        int completed = addCompletedSectionCount();
        int activeIndex = Math.min(completed, steps.length - 1);
        for (int i = 0; i < steps.length; i++) {
            boolean done = addSectionComplete(i);
            boolean active = i == activeIndex || done;
            TextView step = text(steps[i], 10, active ? Color.WHITE : PURPLE, true);
            step.setGravity(Gravity.CENTER);
            step.setPadding(dp(8), dp(7), dp(8), dp(7));
            step.setBackground(round(active ? PURPLE : Color.rgb(247, 241, 255), active ? PURPLE : Color.rgb(224, 208, 242), dp(13), 1));
            final int target = i;
            step.setOnClickListener(v -> Toast.makeText(this, addSectionName(target), Toast.LENGTH_SHORT).show());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(30), 1);
            if (i > 0) lp.leftMargin = dp(6);
            row.addView(step, lp);
        }
        wrap.addView(row);
        String progressText = completed + " / 5 bölmə tamamlandı"; // validator baseline: 0 / 5 bölmə tamamlandı
        TextView note = text(progressText, 12, MUTED, false);
        wrap.addView(note, topLp(dp(10)));
        return wrap;
    }

    private boolean addSectionComplete(int index) {
        switch (index) {
            case 0:
                return !TextUtils.isEmpty(addDraft.vehicleCategory);
            case 1:
                return filled(addDraft.brand, addDraft.model, addDraft.year, addDraft.bodyType, addDraft.fuel,
                        addDraft.drivetrain, addDraft.transmission, addDraft.engineVolume, addDraft.horsepower,
                        addDraft.mileage, addDraft.seats, addDraft.color, addDraft.market);
            case 2:
                return addDraft.imageUris.size() >= MIN_ADD_IMAGES;
            case 3:
                return filled(addDraft.condition, addDraft.city, addDraft.price, addDraft.currency, addDraft.saleType) &&
                        (addDraft.noAccident || addDraft.notRepainted || addDraft.damaged);
            case 4:
                return filled(addDraft.sellerName, normalizeAzPhone(addDraft.phone));
            default:
                return false;
        }
    }

    private int addCompletedSectionCount() {
        int count = 0;
        for (int i = 0; i < 5; i++) if (addSectionComplete(i)) count++;
        return count;
    }

    private String addSectionName(int index) {
        String[] names = {"Avtomobilin növü", "Avtomobil haqqında", "Şəkillər", "Vəziyyət və qiymət", "Əlaqə"};
        return names[Math.max(0, Math.min(index, names.length - 1))];
    }

    private boolean filled(String... values) {
        if (values == null) return false;
        for (String value : values) if (TextUtils.isEmpty(String.valueOf(value == null ? "" : value).trim())) return false;
        return true;
    }

    private LinearLayout addVehicleTypeSection() {
        LinearLayout card = addSectionCard("AVTOMOBİLİN NÖVÜ", "Nə satırsınız?", "Kateqoriyanı seçin.");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] values = {"Minik", "Kommersiya", "Moto"};
        String[] icons = {"🚗", "🚛", "🏍"};
        for (int i = 0; i < values.length; i++) {
            final String value = values[i];
            LinearLayout choice = addChoiceCard(icons[i], value, value.equals(addDraft.vehicleCategory));
            choice.setOnClickListener(v -> {
                addDraft.vehicleCategory = value;
                if (!arrayContains(bodyTypesForCategory(value), addDraft.bodyType)) addDraft.bodyType = "";
                showAddPlaceholder();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(98), 1);
            if (i > 0) lp.leftMargin = dp(8);
            row.addView(choice, lp);
        }
        card.addView(row, topLp(dp(14)));
        return card;
    }

    private LinearLayout addTechnicalSection() {
        LinearLayout card = addSectionCard("MARKA, MODEL VƏ TEXNİKİ MƏLUMATLAR", "Avtomobil haqqında", "Əsas texniki məlumatları doldurun.");

        AutoCompleteTextView brand = autoComplete("Marka seçin və ya yazın", BRANDS);
        brand.setText(addDraft.brand, false);
        AutoCompleteTextView model = autoComplete(TextUtils.isEmpty(addDraft.brand) ? "Əvvəl marka seçin" : "Model seçin və ya yazın", modelsForBrand(addDraft.brand));
        wireAddBrandModel(brand, model);
        model.setText(addDraft.model, false);
        model.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable e) { addDraft.model = e == null ? "" : e.toString().trim(); }
        });
        card.addView(addField("Marka *", brand), topLp(dp(14)));
        card.addView(addField("Model *", model), topLp(dp(12)));

        card.addView(addField("Buraxılış ili *", addSelectValue(emptyToHint(addDraft.year, "İli seçin"), "Buraxılış ili", yearsArray(), addDraft.year, value -> { addDraft.year = value; showAddPlaceholder(); })), topLp(dp(12)));
        card.addView(addField("Ban növü *", addSelectValue(emptyToHint(addDraft.bodyType, "Ban növünü seçin"), "Ban növü", bodyTypesForCategory(addDraft.vehicleCategory), addDraft.bodyType, value -> { addDraft.bodyType = value; showAddPlaceholder(); })), topLp(dp(12)));

        TextView fuelLabel = label("Yanacaq *");
        card.addView(fuelLabel, topLp(dp(16)));
        card.addView(addPillWrap(new String[]{"Benzin", "Dizel", "Hibrid", "Plug-in Hibrid", "Elektro", "Qaz"}, addDraft.fuel, value -> { addDraft.fuel = value; showAddPlaceholder(); }), topLp(dp(8)));

        card.addView(addField("Ötürücü *", addSelectValue(emptyToHint(addDraft.drivetrain, "Ötürücü seçin"), "Ötürücü", DRIVETRAINS, addDraft.drivetrain, value -> { addDraft.drivetrain = value; showAddPlaceholder(); })), topLp(dp(14)));
        card.addView(addField("Sürətlər qutusu *", addSelectValue(emptyToHint(addDraft.transmission, "Sürətlər qutusu seçin"), "Sürətlər qutusu", TRANSMISSIONS, addDraft.transmission, value -> { addDraft.transmission = value; showAddPlaceholder(); })), topLp(dp(12)));
        card.addView(addField("Mühərrikin həcmi, L *", addEngineVolumeInput()), topLp(dp(12)));
        card.addView(addField("Güc, a.q. *", addInput("190", InputType.TYPE_CLASS_NUMBER, addDraft.horsepower, value -> addDraft.horsepower = digitsOnly(value))), topLp(dp(12)));
        card.addView(addField("Yürüş *", addInput("68500", InputType.TYPE_CLASS_NUMBER, addDraft.mileage, value -> addDraft.mileage = digitsOnly(value))), topLp(dp(12)));
        card.addView(addField("Yerlərin sayı *", addSelectValue(emptyToHint(addDraft.seats, "Yerlər"), "Yerlərin sayı", SEATS, addDraft.seats, value -> { addDraft.seats = value; showAddPlaceholder(); })), topLp(dp(12)));

        card.addView(addField("Rəng *", addSelectValue(emptyToHint(addDraft.color, "Rəng seçin"), "Rəng", COLOR_NAMES, addDraft.color, value -> { addDraft.color = value; showAddPlaceholder(); })), topLp(dp(12)));
        card.addView(addColorPalette(), topLp(dp(10)));

        TextView marketLabel = label("Hansı bazar üçün yığılıb? *");
        card.addView(marketLabel, topLp(dp(16)));
        card.addView(addPillWrap(new String[]{"Amerika", "Avropa", "Çin", "Dubay", "Koreya", "Rusiya", "Yaponiya", "Digər"}, addDraft.market, value -> { addDraft.market = value; showAddPlaceholder(); }), topLp(dp(8)));

        return card;
    }

    private LinearLayout addPhotoSection() {
        LinearLayout card = addSectionCard("ŞƏKİLLƏR", "Avtomobil şəkilləri", "Real backend ilə uyğun: minimum 3, maksimum 15 şəkil əlavə ediləcək.");
        LinearLayout photoBox = new LinearLayout(this);
        photoBox.setOrientation(LinearLayout.VERTICAL);
        photoBox.setGravity(Gravity.CENTER);
        photoBox.setPadding(dp(14), dp(18), dp(14), dp(18));
        photoBox.setBackground(round(Color.rgb(251, 250, 253), Color.rgb(214, 207, 226), dp(18), 1));
        TextView icon = text("📷", 30, PURPLE, true);
        icon.setGravity(Gravity.CENTER);
        TextView title = text(addDraft.imageUris.isEmpty() ? "Şəkil seçin" : addDraft.imageUris.size() + "/15 şəkil seçildi", 17, TEXT, true);
        title.setGravity(Gravity.CENTER);
        TextView sub = text("Şəkilləri sola/sağa düzün, istədiyinizi Ana et seçin və ya silin.", 12, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(dp(2), 1.05f);
        photoBox.addView(icon);
        photoBox.addView(title, topLp(dp(8)));
        photoBox.addView(sub, topLp(dp(8)));
        photoBox.setOnClickListener(v -> openAddImagePicker());
        card.addView(photoBox, topLp(dp(14)));
        if (!addDraft.imageUris.isEmpty()) {
            card.addView(addPhotoPreviewGrid(), topLp(dp(12)));
        }
        return card;
    }

    private LinearLayout addPriceConditionSection() {
        LinearLayout card = addSectionCard("VƏZİYYƏT VƏ QİYMƏT", "Qiymət və vəziyyət", "Satış şərtlərini aydın yazın.");
        card.addView(addField("Vəziyyət *", addSelectValue(emptyToHint(addDraft.condition, "Vəziyyəti seçin"), "Vəziyyət", CONDITIONS, addDraft.condition, value -> { addDraft.condition = value; showAddPlaceholder(); })), topLp(dp(14)));
        card.addView(addField("Şəhər/rayon *", addSelectValue(emptyToHint(addDraft.city, "Şəhər seçin"), "Şəhər/rayon", CITIES, addDraft.city, value -> { addDraft.city = value; showAddPlaceholder(); })), topLp(dp(12)));
        card.addView(addField("Qiymət *", addInput("27800", InputType.TYPE_CLASS_NUMBER, addDraft.price, value -> addDraft.price = digitsOnly(value))), topLp(dp(14)));
        TextView currencyLabel = label("Valyuta");
        card.addView(currencyLabel, topLp(dp(14)));
        card.addView(addPillWrap(new String[]{"AZN", "USD", "EUR"}, addDraft.currency, value -> { addDraft.currency = value; showAddPlaceholder(); }), topLp(dp(8)));
        card.addView(addField("Satış tipi *", addSelectValue(emptyToHint(addDraft.saleType, "Satış tipi"), "Satış tipi", SALE_TYPES, addDraft.saleType, value -> { addDraft.saleType = value; showAddPlaceholder(); })), topLp(dp(14)));
        card.addView(addCheckLike("Kreditdədir", addDraft.credit, () -> { addDraft.credit = !addDraft.credit; showAddPlaceholder(); }), topLp(dp(12)));
        card.addView(addCheckLike("Barter mümkündür", addDraft.barter, () -> { addDraft.barter = !addDraft.barter; showAddPlaceholder(); }), topLp(dp(10)));
        card.addView(addCheckLike("Vuruğu yoxdur", addDraft.noAccident, () -> { addDraft.noAccident = !addDraft.noAccident; if (addDraft.noAccident) addDraft.damaged = false; showAddPlaceholder(); }), topLp(dp(10)));
        card.addView(addCheckLike("Rənglənməyib", addDraft.notRepainted, () -> { addDraft.notRepainted = !addDraft.notRepainted; if (addDraft.notRepainted) addDraft.damaged = false; showAddPlaceholder(); }), topLp(dp(10)));
        card.addView(addCheckLike("Qəzalı / hissə üçün", addDraft.damaged, () -> { addDraft.damaged = !addDraft.damaged; if (addDraft.damaged) { addDraft.noAccident = false; addDraft.notRepainted = false; } showAddPlaceholder(); }), topLp(dp(10)));
        card.addView(addCheckLike("VIN-kod mövcuddur", addDraft.vin, () -> { addDraft.vin = !addDraft.vin; showAddPlaceholder(); }), topLp(dp(10)));
        if (addDraft.vin) {
            card.addView(addField("VIN kodu", addInput("VIN kodu yazın", InputType.TYPE_CLASS_TEXT, addDraft.vinCode, value -> addDraft.vinCode = normalizeVin(value))), topLp(dp(12)));
        }
        return card;
    }

    private LinearLayout addContactSection() {
        LinearLayout card = addSectionCard("ƏLAQƏ", "Əlaqə məlumatları", "Alıcıların sizinlə əlaqə saxlaması üçün məlumatları yoxlayın.");
        card.addView(addField("Ad və soyad *", addInput("BYQEZI istifadəçisi", InputType.TYPE_CLASS_TEXT, addDraft.sellerName, value -> addDraft.sellerName = value)), topLp(dp(14)));
        card.addView(addField("Telefon nömrəsi *", addInput("0556208480", InputType.TYPE_CLASS_PHONE, addDraft.phone, value -> addDraft.phone = value)), topLp(dp(12)));
        EditText desc = addInput("Təsvir yazın", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, addDraft.description, value -> addDraft.description = value);
        desc.setSingleLine(false);
        desc.setMinLines(4);
        desc.setTextSize(16);
        desc.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        desc.setGravity(Gravity.TOP | Gravity.START);
        desc.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-1, dp(132));
        LinearLayout field = addField("Təsvir", desc);
        field.removeView(desc);
        field.addView(desc, dlp);
        card.addView(field, topLp(dp(12)));
        return card;
    }

    private LinearLayout addPlainCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));
        return card;
    }

    private LinearLayout addSectionCard(String kicker, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));
        TextView k = text(kicker, 11, PURPLE, true);
        k.setLetterSpacing(0.03f);
        card.addView(k);
        TextView t = text(title, 23, TEXT, true);
        t.setLetterSpacing(-0.015f);
        card.addView(t, topLp(dp(8)));
        if (!TextUtils.isEmpty(subtitle)) {
            TextView s = text(subtitle, 13, MUTED, false);
            s.setLineSpacing(dp(2), 1.05f);
            card.addView(s, topLp(dp(8)));
        }
        return card;
    }

    private LinearLayout addChoiceCard(String icon, String title, boolean selected) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(10), dp(8), dp(10));
        card.setBackground(round(selected ? Color.rgb(248, 241, 255) : Color.WHITE, selected ? PURPLE : BORDER, dp(18), selected ? 2 : 1));
        card.setClickable(true);
        card.setFocusable(true);
        TextView ic = text(icon, 25, TEXT, true);
        ic.setGravity(Gravity.CENTER);
        TextView tx = text(title, 12, selected ? PURPLE : TEXT, true);
        tx.setGravity(Gravity.CENTER);
        card.addView(ic);
        card.addView(tx, topLp(dp(8)));
        return card;
    }

    private LinearLayout addField(String title, View input) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(label(title));
        wrap.addView(input, inputLp());
        return wrap;
    }

    private EditText addInput(String hint, int type) {
        return addInput(hint, type, "", null);
    }

    private EditText addInput(String hint, int type, String value, ValueHandler handler) {
        EditText input = new EditText(this);
        input.setTextSize(17);
        input.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        input.setTextColor(TEXT);
        input.setHintTextColor(LIGHT_MUTED);
        input.setHint(hint);
        input.setSingleLine((type & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        input.setInputType(type);
        input.setPadding(dp(18), 0, dp(18), 0);
        input.setBackground(round(FIELD, BORDER, dp(18), 1));
        if (!TextUtils.isEmpty(value)) {
            input.setText(value);
            input.setSelection(input.getText().length());
        }
        if (handler != null) {
            input.addTextChangedListener(new SimpleWatcher() {
                @Override public void afterTextChanged(Editable e) {
                    handler.onValue(e == null ? "" : e.toString().trim());
                }
            });
        }
        return input;
    }

    private EditText addEngineVolumeInput() {
        EditText input = addInput("2.0 L", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, engineVolumeDisplay(addDraft.engineVolume), null);
        input.addTextChangedListener(new SimpleWatcher() {
            private boolean internal;
            @Override public void afterTextChanged(Editable e) {
                if (internal) return;
                String clean = normalizeEngineVolumeDraft(e == null ? "" : e.toString());
                addDraft.engineVolume = clean;
                String display = engineVolumeDisplay(clean);
                if (!String.valueOf(e == null ? "" : e.toString()).equals(display)) {
                    internal = true;
                    input.setText(display);
                    input.setSelection(Math.min(clean.length(), input.getText().length()));
                    internal = false;
                }
            }
        });
        return input;
    }

    private TextView addSelectLike(String value) {
        TextView view = text(value, 17, TEXT, true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(18), 0, dp(18), 0);
        view.setBackground(round(FIELD, BORDER, dp(18), 1));
        view.setOnClickListener(v -> Toast.makeText(this, value, Toast.LENGTH_SHORT).show());
        return view;
    }

    private TextView addSelectValue(String value, String title, String[] options, String selected, ChoiceHandler handler) {
        boolean hasValue = !TextUtils.isEmpty(selected);
        TextView view = text(value, 17, hasValue ? TEXT : LIGHT_MUTED, true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(18), 0, dp(18), 0);
        view.setBackground(round(FIELD, BORDER, dp(18), 1));
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(v -> showOptionSheet(title, options, selected, handler));
        return view;
    }

    private LinearLayout addPillWrap(String[] values, String selected) {
        return addPillWrap(values, selected, null);
    }

    private LinearLayout addPillWrap(String[] values, String selected, ChoiceHandler handler) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < values.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (i > 0) wrap.addView(row, topLp(dp(8)));
                else wrap.addView(row);
            }
            final String value = values[i];
            TextView pill = addPill(value, value.equalsIgnoreCase(String.valueOf(selected)), handler == null ? null : () -> handler.onChoice(value));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
            if (i % 3 != 0) lp.leftMargin = dp(8);
            row.addView(pill, lp);
        }
        return wrap;
    }

    private TextView addPill(String value, boolean selected) {
        return addPill(value, selected, null);
    }

    private TextView addPill(String value, boolean selected, Runnable action) {
        TextView pill = text(value, 12, selected ? Color.WHITE : Color.rgb(62, 68, 86), true);
        pill.setGravity(Gravity.CENTER);
        pill.setSingleLine(false);
        pill.setBackground(round(selected ? PURPLE : Color.WHITE, selected ? PURPLE : BORDER, dp(18), 1));
        pill.setClickable(true);
        pill.setFocusable(true);
        if (action != null) pill.setOnClickListener(v -> action.run());
        return pill;
    }

    private View addColorPalette() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (i % 7 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (i > 0) wrap.addView(row, topLp(dp(10)));
                else wrap.addView(row);
            }
            final int index = i;
            boolean selected = COLOR_NAMES[index].equals(addDraft.color);
            TextView dot = text("●", 28, COLOR_VALUES[i], true);
            dot.setGravity(Gravity.CENTER);
            dot.setBackground(circle(Color.TRANSPARENT, selected ? PURPLE : Color.TRANSPARENT, selected ? 2 : 0));
            dot.setClickable(true);
            dot.setFocusable(true);
            dot.setOnClickListener(v -> { addDraft.color = COLOR_NAMES[index]; showAddPlaceholder(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(36));
            if (i % 7 != 0) lp.leftMargin = dp(5);
            row.addView(dot, lp);
        }
        return wrap;
    }

    private TextView addCheckLike(String label, boolean selected) {
        return addCheckLike(label, selected, null);
    }

    private TextView addCheckLike(String label, boolean selected, Runnable action) {
        TextView view = text((selected ? "✓  " : "○  ") + label, 14, selected ? PURPLE : TEXT, true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setMinHeight(dp(42));
        view.setPadding(dp(14), 0, dp(14), 0);
        view.setBackground(round(selected ? Color.rgb(248, 241, 255) : Color.WHITE, selected ? Color.rgb(220, 199, 242) : BORDER, dp(15), 1));
        view.setClickable(true);
        view.setFocusable(true);
        if (action != null) view.setOnClickListener(v -> action.run());
        return view;
    }

    private void wireAddBrandModel(AutoCompleteTextView brand, AutoCompleteTextView model) {
        updateAddModelForBrand(brand == null ? "" : brand.getText().toString(), model, false);
        brand.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Object item = parent.getItemAtPosition(position);
                String chosen = item == null ? "" : item.toString();
                addDraft.brand = chosen;
                addDraft.model = "";
                brand.setText(chosen, false);
                brand.setSelection(brand.getText().length());
                updateAddModelForBrand(chosen, model, true);
            } catch (Exception e) {
                Toast.makeText(this, "Marka seçimini yenidən sınayın", Toast.LENGTH_SHORT).show();
            }
        });
        brand.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable e) {
                String value = e == null ? "" : e.toString().trim();
                addDraft.brand = value;
                updateAddModelForBrand(value, model, false);
            }
        });
    }

    private void updateAddModelForBrand(String value, AutoCompleteTextView model, boolean clearModel) {
        if (model == null) return;
        String raw = String.valueOf(value == null ? "" : value).trim();
        String exact = exactBrand(raw);
        if (TextUtils.isEmpty(raw)) {
            model.setEnabled(false);
            model.setAlpha(0.62f);
            model.setHint("Əvvəl marka seçin");
            setAdapter(model, new String[0]);
            if (clearModel) {
                model.setText("", false);
                addDraft.model = "";
            }
            return;
        }
        model.setEnabled(true);
        model.setAlpha(1f);
        model.setHint("Model seçin və ya yazın");
        String[] options = TextUtils.isEmpty(exact) ? new String[]{"Digər"} : modelsForBrand(exact);
        if (options.length == 0) options = new String[]{"Digər"};
        setAdapter(model, options);
        if (clearModel) {
            model.setText("", false);
            addDraft.model = "";
        }
    }

    private void showOptionSheet(String title, String[] options, String selected, ChoiceHandler handler) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.argb(180, 0, 0, 0));
        overlay.setPadding(dp(16), dp(24), dp(16), dp(24));

        LinearLayout panel = modalPanel();
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.addView(text(title, 22, TEXT, true));
        TextView hint = text("Seçimi edin", 12, MUTED, false);
        panel.addView(hint, topLp(dp(6)));

        ScrollView optionsScroll = new ScrollView(this);
        optionsScroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        String[] safeOptions = options == null ? new String[0] : options;
        for (String option : safeOptions) {
            final String value = option;
            boolean active = !TextUtils.isEmpty(selected) && value.equalsIgnoreCase(selected);
            TextView item = text((active ? "✓  " : "") + value, 15, active ? PURPLE : TEXT, true);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setMinHeight(dp(50));
            item.setPadding(dp(14), 0, dp(14), 0);
            item.setBackground(round(active ? Color.rgb(248, 241, 255) : Color.WHITE, active ? PURPLE : BORDER, dp(14), 1));
            item.setOnClickListener(v -> {
                if (handler != null) handler.onChoice(value);
                dialog.dismiss();
            });
            list.addView(item, topLp(dp(8)));
        }
        optionsScroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        int maxHeight = safeOptions.length > 7 ? dp(460) : -2;
        panel.addView(optionsScroll, new LinearLayout.LayoutParams(-1, maxHeight));

        TextView close = button("Bağla", Color.WHITE, PURPLE, BORDER);
        close.setOnClickListener(v -> dialog.dismiss());
        panel.addView(close, topLp(dp(14)));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        lp.leftMargin = dp(10);
        lp.rightMargin = dp(10);
        overlay.addView(panel, lp);
        overlay.setOnClickListener(v -> dialog.dismiss());
        panel.setOnClickListener(v -> { });

        dialog.setContentView(overlay);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private LinearLayout addEquipmentGrid() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < EQUIPMENT.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                if (i > 0) wrap.addView(row, topLp(dp(8)));
                else wrap.addView(row);
            }
            final String item = EQUIPMENT[i];
            TextView chip = addCheckLike(item, addDraft.equipment.contains(item), () -> { toggleDraftEquipment(item); showAddPlaceholder(); });
            chip.setTextSize(11);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(46), 1);
            if (i % 2 != 0) lp.leftMargin = dp(8);
            row.addView(chip, lp);
        }
        return wrap;
    }

    private LinearLayout addPhotoPreviewGrid() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < addDraft.imageUris.size(); i += 3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 3; j++) {
                int index = i + j;
                if (index >= addDraft.imageUris.size()) {
                    Space space = new Space(this);
                    LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, dp(132), 1);
                    if (j > 0) slp.leftMargin = dp(8);
                    row.addView(space, slp);
                    continue;
                }
                FrameLayout tile = new FrameLayout(this);
                tile.setBackground(round(Color.WHITE, index == 0 ? PURPLE : BORDER, dp(14), index == 0 ? 2 : 1));
                ImageView img = new ImageView(this);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                try { img.setImageURI(Uri.parse(addDraft.imageUris.get(index))); } catch (Exception ignored) { }
                roundClip(img, dp(14));
                tile.addView(img, new FrameLayout.LayoutParams(-1, dp(132)));

                TextView tag = text(index == 0 ? "ANA" : "Şəkil " + (index + 1), 9, Color.WHITE, true);
                tag.setGravity(Gravity.CENTER);
                tag.setPadding(dp(6), dp(3), dp(6), dp(3));
                tag.setBackground(round(Color.argb(165, 92, 16, 172), Color.TRANSPARENT, dp(10), 0));
                FrameLayout.LayoutParams tagLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
                tagLp.leftMargin = dp(5);
                tagLp.topMargin = dp(5);
                tile.addView(tag, tagLp);

                TextView del = text("×", 16, Color.WHITE, true);
                del.setGravity(Gravity.CENTER);
                del.setBackground(circle(Color.argb(190, 20, 20, 28), Color.TRANSPARENT, 0));
                final int removeIndex = index;
                del.setOnClickListener(v -> { if (removeIndex >= 0 && removeIndex < addDraft.imageUris.size()) { addDraft.imageUris.remove(removeIndex); showAddPlaceholder(); } });
                FrameLayout.LayoutParams delLp = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.TOP | Gravity.RIGHT);
                delLp.rightMargin = dp(5);
                delLp.topMargin = dp(5);
                tile.addView(del, delLp);

                LinearLayout controls = new LinearLayout(this);
                controls.setOrientation(LinearLayout.HORIZONTAL);
                controls.setGravity(Gravity.CENTER);
                controls.setPadding(dp(4), dp(4), dp(4), dp(4));
                controls.setBackground(round(Color.argb(178, 20, 20, 28), Color.TRANSPARENT, dp(12), 0));

                TextView left = photoControl("‹", index > 0);
                left.setOnClickListener(v -> moveDraftImage(removeIndex, removeIndex - 1));
                TextView mainBtn = photoControl(index == 0 ? "Ana" : "Ana et", true);
                mainBtn.setTextSize(index == 0 ? 9 : 8);
                mainBtn.setOnClickListener(v -> makeDraftImageMain(removeIndex));
                TextView right = photoControl("›", index < addDraft.imageUris.size() - 1);
                right.setOnClickListener(v -> moveDraftImage(removeIndex, removeIndex + 1));

                controls.addView(left, new LinearLayout.LayoutParams(dp(28), dp(28)));
                LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(0, dp(28), 1);
                midLp.leftMargin = dp(3);
                midLp.rightMargin = dp(3);
                controls.addView(mainBtn, midLp);
                controls.addView(right, new LinearLayout.LayoutParams(dp(28), dp(28)));

                FrameLayout.LayoutParams controlsLp = new FrameLayout.LayoutParams(-1, dp(38), Gravity.BOTTOM);
                controlsLp.leftMargin = dp(5);
                controlsLp.rightMargin = dp(5);
                controlsLp.bottomMargin = dp(5);
                tile.addView(controls, controlsLp);

                tile.setOnClickListener(v -> makeDraftImageMain(removeIndex));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(132), 1);
                if (j > 0) lp.leftMargin = dp(8);
                row.addView(tile, lp);
            }
            if (i > 0) wrap.addView(row, topLp(dp(8)));
            else wrap.addView(row);
        }
        TextView addMore = button("+ Şəkil əlavə edin", Color.WHITE, PURPLE, BORDER);
        addMore.setTextSize(13);
        addMore.setOnClickListener(v -> openAddImagePicker());
        wrap.addView(addMore, topLp(dp(10)));
        return wrap;
    }

    private TextView photoControl(String label, boolean enabled) {
        TextView b = text(label, 10, enabled ? Color.WHITE : Color.argb(115, 255, 255, 255), true);
        b.setGravity(Gravity.CENTER);
        b.setEnabled(enabled);
        b.setBackground(round(enabled ? Color.argb(95, 255, 255, 255) : Color.TRANSPARENT, Color.TRANSPARENT, dp(9), 0));
        return b;
    }

    private void moveDraftImage(int from, int to) {
        if (from < 0 || from >= addDraft.imageUris.size() || to < 0 || to >= addDraft.imageUris.size() || from == to) return;
        String uri = addDraft.imageUris.remove(from);
        addDraft.imageUris.add(to, uri);
        showAddPlaceholder();
    }

    private void makeDraftImageMain(int index) {
        if (index <= 0 || index >= addDraft.imageUris.size()) return;
        String uri = addDraft.imageUris.remove(index);
        addDraft.imageUris.add(0, uri);
        showAddPlaceholder();
    }

    private void openAddImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Şəkil seçin"), REQ_ADD_IMAGES);
        } catch (Exception e) {
            Toast.makeText(this, "Şəkil seçimi açıla bilmədi", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAddListing() {
        hideKeyboard();
        ensureAddDraftContactDefaults();
        String error = validateAddDraft();
        if (!TextUtils.isEmpty(error)) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            return;
        }
        if (addSubmitting) return;
        addSubmitting = true;
        Toast.makeText(this, "Şəkillər yüklənir və elan göndərilir...", Toast.LENGTH_LONG).show();
        final AddListingDraft draft = addDraft.copy();
        executor.execute(() -> {
            try {
                ArrayList<String> urls = uploadListingImages(draft.imageUris);
                JSONObject body = listingJsonFromDraft(draft, urls);
                JSONObject res = postJson("/api/listings", body);
                JSONObject listingObj = res.optJSONObject("listing");
                Listing listing = Listing.fromJson(listingObj, 0);
                listing.mine = true;
                listing.status = TextUtils.isEmpty(listing.status) ? "pending" : listing.status;
                main.post(() -> {
                    addSubmitting = false;
                    userListings.add(0, listing);
                    listings.add(0, listing);
                    addDraft = new AddListingDraft();
                    cabinetFilter = "pending";
                    userListingsLastFetchMs = 0L;
                    Toast.makeText(this, res.optString("message", "Elan admin təsdiqinə göndərildi"), Toast.LENGTH_LONG).show();
                    fetchListings();
                    refreshUserListings(true);
                    showCabinet();
                });
            } catch (Exception e) {
                main.post(() -> {
                    addSubmitting = false;
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private JSONObject listingJsonFromDraft(AddListingDraft draft, ArrayList<String> imageUrls) throws Exception {
        JSONObject body = new JSONObject();
        body.put("_user_submit", true);
        body.put("vehicle_category", draft.vehicleCategory);
        body.put("brand", draft.brand);
        body.put("model", draft.model);
        body.put("year", cleanInt(draft.year));
        body.put("body_type", draft.bodyType);
        body.put("fuel", draft.fuel);
        body.put("drivetrain", draft.drivetrain);
        body.put("transmission", draft.transmission);
        body.put("engine_volume", normalizeEngineVolumeDraft(draft.engineVolume));
        body.put("engine", engineVolumeDisplay(draft.engineVolume));
        body.put("horsepower", cleanInt(draft.horsepower));
        body.put("mileage", cleanInt(draft.mileage));
        body.put("seats", draft.seats);
        body.put("color", draft.color);
        body.put("market", draft.market);
        body.put("condition", draft.condition);
        body.put("city", draft.city);
        body.put("price", cleanInt(draft.price));
        body.put("currency", draft.currency);
        body.put("sale_type", draft.saleType);
        body.put("credit", draft.credit);
        body.put("barter", draft.barter);
        body.put("no_accident", draft.noAccident);
        body.put("not_repainted", draft.notRepainted);
        body.put("damaged", draft.damaged);
        body.put("vin", draft.vin);
        body.put("vin_code", draft.vinCode);
        body.put("seller_name", draft.sellerName);
        body.put("phone", normalizeAzPhone(draft.phone));
        body.put("whatsapp", normalizeAzPhone(draft.phone));
        body.put("description", draft.description);
        JSONArray images = new JSONArray();
        for (String url : imageUrls) images.put(url);
        body.put("images", images);
        JSONArray equipment = new JSONArray();
        for (String item : draft.equipment) equipment.put(item);
        body.put("equipment", equipment);
        return body;
    }

    private String validateAddDraft() {
        if (!filled(addDraft.vehicleCategory)) return "Avtomobilin növünü seçin";
        if (!filled(addDraft.brand)) return "Marka seçin və ya yazın";
        if (!filled(addDraft.model)) return "Model seçin və ya yazın";
        if (!filled(addDraft.year)) return "Buraxılış ilini seçin";
        if (!filled(addDraft.bodyType)) return "Ban növünü seçin";
        if (!filled(addDraft.fuel)) return "Yanacaq növünü seçin";
        if (!filled(addDraft.drivetrain)) return "Ötürücünü seçin";
        if (!filled(addDraft.transmission)) return "Sürətlər qutusunu seçin";
        if (!filled(addDraft.engineVolume)) return "Mühərrikin həcmini yazın";
        if (!filled(addDraft.horsepower)) return "Gücü yazın";
        if (!filled(addDraft.mileage)) return "Yürüşü yazın";
        if (!filled(addDraft.seats)) return "Yerlərin sayını seçin";
        if (!filled(addDraft.color)) return "Rəngi seçin";
        if (!filled(addDraft.market)) return "Bazar seçimini edin";
        if (addDraft.imageUris.size() < MIN_ADD_IMAGES) return "Ən azı 3 şəkil seçin";
        if (!filled(addDraft.condition)) return "Vəziyyəti seçin";
        if (!filled(addDraft.city)) return "Şəhər/rayon seçin";
        if (!filled(addDraft.price) || cleanInt(addDraft.price) <= 0) return "Qiyməti düzgün yazın";
        if (!filled(addDraft.saleType)) return "Satış tipini seçin";
        if (!(addDraft.noAccident || addDraft.notRepainted || addDraft.damaged)) return "Qəza/rəng vəziyyətindən ən azı bir seçim edin";
        if (!filled(addDraft.sellerName)) return "Ad və soyadı yazın";
        if (TextUtils.isEmpty(normalizeAzPhone(addDraft.phone))) return "Telefon nömrəsini düzgün yazın: 0556208480";
        return "";
    }

    private Listing listingFromDraft() {
        Listing l = new Listing();
        l.id = "local-" + System.currentTimeMillis();
        l.brand = addDraft.brand.trim();
        l.model = addDraft.model.trim();
        l.title = (l.brand + " " + l.model).trim();
        l.year = addDraft.year;
        l.price = cleanInt(addDraft.price);
        l.currency = addDraft.currency;
        l.city = addDraft.city;
        l.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        l.time = "indi";
        l.engine = engineVolumeDisplay(addDraft.engineVolume);
        l.mileage = mileageDisplay(addDraft.mileage);
        l.localImage = 0;
        l.imageUrl = addDraft.imageUris.isEmpty() ? "" : addDraft.imageUris.get(0);
        l.imageUris.addAll(addDraft.imageUris);
        l.vip = false;
        l.premium = false;
        l.status = "pending";
        l.mine = true;
        l.sellerName = addDraft.sellerName;
        l.phone = normalizeAzPhone(addDraft.phone);
        l.bodyType = addDraft.bodyType;
        l.color = addDraft.color;
        l.transmission = addDraft.transmission;
        l.drivetrain = addDraft.drivetrain;
        l.condition = addDraft.condition;
        l.market = addDraft.market;
        l.seats = addDraft.seats;
        l.accidentPaint = composeAccidentPaint();
        l.description = addDraft.description;
        l.horsepower = addDraft.horsepower;
        l.credit = addDraft.credit;
        l.barter = addDraft.barter;
        l.equipment.addAll(addDraft.equipment);
        return l;
    }

    private String composeAccidentPaint() {
        ArrayList<String> parts = new ArrayList<>();
        if (addDraft.noAccident) parts.add("Vuruğu yoxdur");
        if (addDraft.notRepainted) parts.add("Rənglənməyib");
        if (addDraft.damaged) parts.add("Qəzalı / hissə üçün");
        return TextUtils.join(", ", parts);
    }

    private void toggleDraftEquipment(String item) {
        if (addDraft.equipment.contains(item)) addDraft.equipment.remove(item);
        else addDraft.equipment.add(item);
    }

    private String emptyToHint(String value, String hint) {
        return TextUtils.isEmpty(value) ? hint : value;
    }

    private String[] yearsArray() {
        ArrayList<String> list = new ArrayList<>();
        for (int y = 2026; y >= 1906; y--) list.add(String.valueOf(y));
        return list.toArray(new String[0]);
    }

    private String[] bodyTypesForCategory(String category) {
        if ("Kommersiya".equalsIgnoreCase(category)) return new String[]{"Yük maşını", "Furqon", "Van", "Mikroavtobus", "Avtobus", "Avtokran", "Dartqı", "Pikap, tək kabin", "Pikap, bir yarım kabin", "Pikap, ikiqat kabin"};
        if ("Moto".equalsIgnoreCase(category)) return new String[]{"Motosiklet", "Moped", "Skuter", "Trisikl", "Kvadrosikl", "Qolfkar"};
        return new String[]{"Sedan", "Hetçbek, 3 qapı", "Hetçbek, 4 qapı", "Hetçbek, 5 qapı", "Fastbek", "Liftbek", "Universal, 3 qapı", "Universal, 5 qapı", "Kupe", "Kabriolet", "Rodster", "Tarqa", "Limuzin", "Minivan", "Mikrovan", "Kompakt-Van", "Van", "SUV", "SUV Kupe", "Offroader / SUV, 3 qapı", "Offroader / SUV, 5 qapı", "Offroader / SUV, açıq", "Pikap, tək kabin", "Pikap, bir yarım kabin", "Pikap, ikiqat kabin"};
    }

    private boolean arrayContains(String[] values, String target) {
        if (values == null || TextUtils.isEmpty(target)) return false;
        for (String value : values) if (target.equals(value)) return true;
        return false;
    }

    private String digitsOnly(String value) {
        return String.valueOf(value == null ? "" : value).replaceAll("[^0-9]", "");
    }

    private int cleanInt(String value) {
        try { return Integer.parseInt(digitsOnly(value)); } catch (Exception ignored) { return 0; }
    }

    private String normalizeEngineVolumeDraft(String value) {
        String raw = String.valueOf(value == null ? "" : value).replace(',', '.').replaceAll("[^0-9.]", "");
        StringBuilder out = new StringBuilder();
        boolean dot = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '.') {
                if (!dot) { out.append(c); dot = true; }
            } else out.append(c);
        }
        return out.toString();
    }

    private String engineVolumeDisplay(String value) {
        String clean = normalizeEngineVolumeDraft(value);
        if (TextUtils.isEmpty(clean)) return "";
        if (clean.endsWith(".")) clean = clean.substring(0, clean.length() - 1);
        return clean + " L";
    }

    private String mileageDisplay(String value) {
        int n = cleanInt(value);
        if (n <= 0) return "";
        try { return NumberFormat.getIntegerInstance(new Locale("az", "AZ")).format(n).replace(',', ' ') + " km"; }
        catch (Exception e) { return n + " km"; }
    }

    private String normalizeVin(String value) {
        String v = String.valueOf(value == null ? "" : value).toUpperCase(Locale.ROOT).replaceAll("[^A-HJ-NPR-Z0-9]", "");
        return v.length() > 20 ? v.substring(0, 20) : v;
    }

    private void syncUserListingsFromLocal() {
        userListings.clear();
        for (Listing row : listings) if (row.mine) userListings.add(row);
    }

    private void refreshUserListings(boolean force) {
        if (!userLoggedIn || userListingsLoading) return;
        long now = System.currentTimeMillis();
        if (!force && userListingsLastFetchMs > 0 && now - userListingsLastFetchMs < 7000) return;
        userListingsLoading = true;
        executor.execute(() -> {
            try {
                ArrayList<Listing> mine = requestUserListings();
                main.post(() -> {
                    userListingsLoading = false;
                    userListingsLastFetchMs = System.currentTimeMillis();
                    userListings.clear();
                    for (Listing row : mine) { row.mine = true; userListings.add(row); }
                    if ("cabinet".equals(activeScreen)) showCabinet();
                });
            } catch (Exception e) {
                main.post(() -> {
                    userListingsLoading = false;
                    userListingsLastFetchMs = System.currentTimeMillis();
                });
            }
        });
    }

    private void clearNativeSession() {
        userLoggedIn = false;
        userPhone = "";
        userName = "";
        otpChallengeId = "";
        otpPhone = "";
        authLoading = false;
        userListings.clear();
        userListingsLastFetchMs = 0L;
        cabinetFilter = "all";
        try {
            nativeCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(nativeCookieManager);
            getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply();
        } catch (Exception ignored) { }
    }

    private void restoreNativeSession() {
        try {
            nativeCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            SharedPreferences prefs = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
            userLoggedIn = prefs.getBoolean(PREF_LOGGED_IN, false);
            userName = prefs.getString(PREF_USER_NAME, "");
            userPhone = prefs.getString(PREF_USER_PHONE, "");
            if (TextUtils.isEmpty(normalizeAzPhone(userPhone))) {
                userLoggedIn = false;
                userPhone = "";
            }
            restoreCookiesFromJson(prefs.getString(PREF_COOKIES, ""));
            CookieHandler.setDefault(nativeCookieManager);
        } catch (Exception ignored) {
            nativeCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(nativeCookieManager);
            userLoggedIn = false;
            userName = "";
            userPhone = "";
        }
    }

    private void saveNativeSession() {
        try {
            getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)
                    .edit()
                    .putBoolean(PREF_LOGGED_IN, userLoggedIn)
                    .putString(PREF_USER_NAME, safe(userName, ""))
                    .putString(PREF_USER_PHONE, normalizeAzPhone(userPhone))
                    .putString(PREF_COOKIES, cookiesToJson())
                    .putString(PREF_PENDING_PROMOTION_ORDER, safe(pendingPromotionOrderId, ""))
                    .putString(PREF_PENDING_PROMOTION_LISTING, safe(pendingPromotionListingId, ""))
                    .apply();
        } catch (Exception ignored) { }
    }

    private String cookiesToJson() {
        JSONArray arr = new JSONArray();
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return arr.toString();
            List<HttpCookie> cookies = nativeCookieManager.getCookieStore().getCookies();
            if (cookies == null) return arr.toString();
            for (HttpCookie cookie : cookies) {
                if (cookie == null || cookie.hasExpired()) continue;
                JSONObject obj = new JSONObject();
                obj.put("name", safe(cookie.getName(), ""));
                obj.put("value", safe(cookie.getValue(), ""));
                obj.put("domain", safe(cookie.getDomain(), ""));
                obj.put("path", safe(cookie.getPath(), "/"));
                obj.put("maxAge", cookie.getMaxAge());
                obj.put("secure", cookie.getSecure());
                obj.put("version", cookie.getVersion());
                arr.put(obj);
            }
        } catch (Exception ignored) { }
        return arr.toString();
    }

    private void restoreCookiesFromJson(String json) {
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null || TextUtils.isEmpty(json)) return;
            JSONArray arr = new JSONArray(json);
            URI baseUri = URI.create(BuildConfig.API_BASE_URL);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                String name = obj.optString("name", "");
                String value = obj.optString("value", "");
                if (TextUtils.isEmpty(name)) continue;
                HttpCookie cookie = new HttpCookie(name, value);
                String domain = obj.optString("domain", "");
                String path = obj.optString("path", "/");
                if (!TextUtils.isEmpty(domain)) cookie.setDomain(domain);
                cookie.setPath(TextUtils.isEmpty(path) ? "/" : path);
                cookie.setMaxAge(obj.optLong("maxAge", -1L));
                cookie.setSecure(obj.optBoolean("secure", false));
                cookie.setVersion(obj.optInt("version", 0));
                nativeCookieManager.getCookieStore().add(baseUri, cookie);
            }
        } catch (Exception ignored) { }
    }

    private String normalizeAzPhone(String raw) {
        String digits = String.valueOf(raw == null ? "" : raw).replaceAll("[^0-9]", "");
        if (digits.startsWith("994")) digits = "0" + digits.substring(3);
        if (digits.length() == 9 && !digits.startsWith("0")) digits = "0" + digits;
        if (!digits.matches("0(10|50|51|55|60|70|77|99)[0-9]{7}")) return "";
        return digits;
    }

    private String normalizePhoneDisplay(String raw) {
        String digits = normalizeAzPhone(raw);
        if (TextUtils.isEmpty(digits)) return safe(raw, "0556208480");
        return digits;
    }

    private void showInfo(String title, String body) {
        activeScreen = "info";
        updateNav();
        setDetailMode(false, null);
        content.removeAllViews();
        content.setPadding(dp(14), dp(20), dp(14), dp(28));
        TextView back = text("‹ Ana səhifə", 15, PURPLE, true);
        back.setOnClickListener(v -> showHome(false));
        content.addView(back);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(20));
        card.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        card.addView(text(title, 26, TEXT, true));
        TextView paragraph = text(body, 15, MUTED, false);
        paragraph.setLineSpacing(dp(2), 1.1f);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = dp(12);
        card.addView(paragraph, bp);
        content.addView(card, topLp(18));
    }

    private void showDetail(Listing listing) {
        showDetailInternal(listing, true);
    }

    private void showDetailInternal(Listing listing, boolean refreshFromServer) {
        if (listing == null) return;
        if (currentDetailListing == null || !listingKey(currentDetailListing).equals(listingKey(listing))) {
            detailImageIndex = 0;
        }
        currentDetailListing = listing;
        activeScreen = "detail";
        updateNav();
        setDetailMode(true, listing);
        content.removeAllViews();
        content.setPadding(dp(14), dp(12), dp(14), dp(26));
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        content.addView(detailGalleryCard(listing), topLp(0));
        content.addView(detailInfoCard(listing), topLp(14));

        TextView relatedTitle = text("Oxşar elanlar", 22, TEXT, true);
        content.addView(relatedTitle, topLp(24));
        ArrayList<Listing> related = relatedRows(listing);
        addGrid(content, related, Math.min(related.size(), 6), dp(12));

        if (refreshFromServer) refreshListingDetail(listing);
    }

    private void refreshListingDetail(Listing listing) {
        if (listing == null || TextUtils.isEmpty(listing.id) || listing.id.startsWith("local-")) return;
        final String key = listingKey(listing);
        executor.execute(() -> {
            try {
                Listing fresh = requestListingDetail(listing.id);
                if (fresh == null) return;
                fresh.mine = listing.mine || fresh.mine;
                main.post(() -> {
                    replaceListingInMemory(fresh);
                    if ("detail".equals(activeScreen)
                            && currentDetailListing != null
                            && key.equals(listingKey(currentDetailListing))) {
                        showDetailInternal(fresh, false);
                    }
                });
            } catch (Exception ignored) { }
        });
    }

    private void replaceListingInMemory(Listing updated) {
        if (updated == null) return;
        replaceListingInList(listings, updated);
        replaceListingInList(premiumListings, updated);
        replaceListingInList(vipListings, updated);
        replaceListingInList(userListings, updated);
    }

    private void replaceListingInList(ArrayList<Listing> list, Listing updated) {
        if (list == null || updated == null) return;
        String key = listingKey(updated);
        for (int i = 0; i < list.size(); i++) {
            if (key.equals(listingKey(list.get(i)))) {
                list.set(i, updated);
                return;
            }
        }
    }

    private View detailGalleryCard(Listing listing) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(round(Color.WHITE, Color.TRANSPARENT, dp(14), 0));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView back = text("‹", 28, TEXT, true);
        back.setGravity(Gravity.CENTER);
        back.setBackground(round(Color.rgb(248, 247, 252), BORDER, dp(11), 1));
        back.setOnClickListener(v -> showHome(false));
        top.addView(back, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView title = text("BYQEZI.AZ", 15, PURPLE, true);
        title.setGravity(Gravity.CENTER);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(38), 1));
        TextView fav = text(isFavorite(listing) ? "♥" : "♡", 20, isFavorite(listing) ? Color.rgb(255, 31, 60) : TEXT, true);
        fav.setGravity(Gravity.CENTER);
        fav.setBackground(round(Color.rgb(248, 247, 252), Color.TRANSPARENT, dp(11), 0));
        fav.setOnClickListener(v -> toggleFavorite(listing, fav));
        top.addView(fav, new LinearLayout.LayoutParams(dp(38), dp(38)));
        card.addView(top, new LinearLayout.LayoutParams(-1, dp(54)));

        FrameLayout media = new FrameLayout(this);
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindDetailImage(listing, img, detailImageIndex);
        img.setOnClickListener(v -> showLightbox(listing, detailImageIndex));
        media.addView(img, new FrameLayout.LayoutParams(-1, dp(250)));
        if (listing.isVipActive()) {
            TextView badge = tinyBadge("VIP", PURPLE, Color.WHITE);
            FrameLayout.LayoutParams bLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
            bLp.leftMargin = dp(8);
            bLp.topMargin = dp(8);
            media.addView(badge, bLp);
        }
        TextView prev = galleryNavButton("‹");
        prev.setOnClickListener(v -> moveDetailImage(listing, -1));
        FrameLayout.LayoutParams prevLp = new FrameLayout.LayoutParams(dp(42), dp(54), Gravity.CENTER_VERTICAL | Gravity.LEFT);
        prevLp.leftMargin = dp(8);
        media.addView(prev, prevLp);
        TextView next = galleryNavButton("›");
        next.setOnClickListener(v -> moveDetailImage(listing, 1));
        FrameLayout.LayoutParams nextLp = new FrameLayout.LayoutParams(dp(42), dp(54), Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        nextLp.rightMargin = dp(8);
        media.addView(next, nextLp);
        TextView count = text((detailImageIndex + 1) + "/" + detailImageCount(listing), 11, Color.WHITE, true);
        count.setGravity(Gravity.CENTER);
        count.setPadding(dp(8), dp(4), dp(8), dp(4));
        count.setBackground(round(Color.argb(170, 45, 45, 52), Color.TRANSPARENT, dp(10), 0));
        FrameLayout.LayoutParams countLp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.RIGHT);
        countLp.rightMargin = dp(8);
        countLp.bottomMargin = dp(8);
        media.addView(count, countLp);
        card.addView(media, new LinearLayout.LayoutParams(-1, dp(250)));

        LinearLayout thumbs = new LinearLayout(this);
        thumbs.setOrientation(LinearLayout.HORIZONTAL);
        thumbs.setPadding(dp(10), dp(10), dp(10), dp(10));
        for (int i = 0; i < detailImageCount(listing); i++) {
            final int index = i;
            ImageView thumb = new ImageView(this);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            bindDetailImage(listing, thumb, index);
            thumb.setBackground(round(Color.WHITE, index == detailImageIndex ? PURPLE : BORDER, dp(8), index == detailImageIndex ? 2 : 1));
            thumb.setOnClickListener(v -> { detailImageIndex = index; showDetailInternal(listing, false); });
            roundClip(thumb, dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(72), dp(52));
            if (i > 0) lp.leftMargin = dp(10);
            thumbs.addView(thumb, lp);
        }
        card.addView(thumbs, new LinearLayout.LayoutParams(-1, dp(72)));
        return card;
    }

    private View detailInfoCard(Listing listing) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(2));

        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView price = text(formatMoney(listing.price, listing.currency), 25, TEXT, true);
        priceRow.addView(price, new LinearLayout.LayoutParams(0, -2, 1));
        TextView history = text("Qiymət tarixçəsi", 10, PURPLE, false);
        history.setGravity(Gravity.CENTER);
        history.setPadding(dp(10), dp(7), dp(10), dp(7));
        history.setBackground(round(Color.WHITE, TEXT, dp(14), 1));
        history.setOnClickListener(v -> showPriceHistory(listing));
        priceRow.addView(history, new LinearLayout.LayoutParams(-2, dp(32)));
        card.addView(priceRow);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams chipsLp = new LinearLayout.LayoutParams(-1, -2);
        chipsLp.topMargin = dp(14);
        if (listing.credit) {
            chips.addView(detailChip("Kredit mümkündür", Color.rgb(230, 246, 255), Color.rgb(0, 93, 140), Color.rgb(166, 218, 245)));
        }
        if (listing.barter) {
            TextView barter = detailChip("Barter mümkündür", Color.rgb(229, 250, 240), Color.rgb(0, 132, 78), Color.rgb(157, 227, 190));
            LinearLayout.LayoutParams barterLp = new LinearLayout.LayoutParams(-2, -2);
            if (chips.getChildCount() > 0) barterLp.leftMargin = dp(8);
            chips.addView(barter, barterLp);
        }
        if (chips.getChildCount() > 0) card.addView(chips, chipsLp);

        TextView title = text(detailTitle(listing), 19, TEXT, true);
        title.setLineSpacing(0, 1.05f);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(16);
        card.addView(title, titleLp);

        card.addView(detailDivider(), topLp(dp(14)));
        card.addView(specRow("Şəhər", safe(listing.city, "—")));
        card.addView(specRow("Marka", safe(listing.brand, "—")));
        card.addView(specRow("Model", safe(listing.model, "—")));
        card.addView(specRow("Buraxılış ili", safe(listing.year, "—")));
        card.addView(specRow("Ban növü", safe(listing.bodyType, "—")));
        card.addView(specRow("Rəng", safe(listing.color, "—")));
        card.addView(specRow("Mühərrik", detailEngine(listing)));
        card.addView(specRow("Yürüş", safe(listing.mileage, "—")));
        card.addView(specRow("Sürətlər qutusu", safe(listing.transmission, "—")));
        card.addView(specRow("Ötürücü", safe(listing.drivetrain, "—")));
        card.addView(specRow("Vəziyyət", safe(listing.condition, "—")));
        card.addView(specRow("Hansı bazar üçün\nyığılıb", safe(listing.market, "—")));
        card.addView(specRow("Yerlərin sayı", safe(listing.seats, "—")));
        card.addView(specRow("Qəza/rəng vəziyyəti", safe(listing.accidentPaint, "—")));

        if (!TextUtils.isEmpty(listing.description)) {
            TextView desc = text(listing.description, 13, Color.rgb(76, 74, 88), false);
            desc.setLineSpacing(dp(2), 1.05f);
            LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(-1, -2);
            descLp.topMargin = dp(14);
            card.addView(desc, descLp);
        }

        if (!listing.equipment.isEmpty()) {
            LinearLayout featureRow = new LinearLayout(this);
            featureRow.setOrientation(LinearLayout.HORIZONTAL);
            featureRow.setPadding(0, dp(12), 0, 0);
            int featureLimit = Math.min(4, listing.equipment.size());
            for (int i = 0; i < featureLimit; i++) {
                String f = listing.equipment.get(i);
                if (TextUtils.isEmpty(f)) continue;
                TextView chip = text(f, 10, TEXT, false);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(9), dp(6), dp(9), dp(6));
                chip.setBackground(round(Color.WHITE, BORDER, dp(12), 1));
                LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(-2, -2);
                fLp.rightMargin = dp(6);
                featureRow.addView(chip, fLp);
            }
            if (featureRow.getChildCount() > 0) card.addView(featureRow);
        }

        if (isOwnedListing(listing) && listing.isActiveStatus()) {
            card.addView(promotionQuickRow(listing), topLp(dp(14)));
        }

        card.addView(sellerCard(listing), topLp(dp(14)));
        card.addView(complaintCard(listing), topLp(dp(12)));

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.HORIZONTAL);
        meta.setPadding(0, dp(14), 0, 0);
        meta.addView(smallMeta("Elanın nömrəsi " + safe(listing.id, "—")));
        meta.addView(smallMeta("Baxışların sayı " + formatCount(listing.views)));
        card.addView(meta);
        if (!TextUtils.isEmpty(listing.createdAt)) {
            TextView date = text("Yerləşdirildi: " + listing.fullDateText(), 11, MUTED, false);
            LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(-1, -2);
            dateLp.topMargin = dp(8);
            card.addView(date, dateLp);
        }
        return card;
    }

    private LinearLayout sellerCard(Listing listing) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(14), dp(14), dp(14));
        box.setBackground(round(Color.WHITE, BORDER, dp(14), 1));
        TextView name = text(safe(listing.sellerName, "Satıcı"), 14, TEXT, true);
        box.addView(name);
        TextView phone = text(safe(listing.phone, "Telefon gizlidir"), 13, TEXT, true);
        LinearLayout.LayoutParams phoneLp = new LinearLayout.LayoutParams(-1, -2);
        phoneLp.topMargin = dp(4);
        box.addView(phone, phoneLp);
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);
        TextView call = button("☎ Zəng edin", Color.rgb(20, 190, 112), Color.WHITE, Color.TRANSPARENT);
        call.setTextSize(13);
        call.setOnClickListener(v -> dialPhone(listing));
        TextView whats = button("WhatsApp", Color.rgb(20, 190, 112), Color.rgb(5, 72, 42), Color.TRANSPARENT);
        whats.setTextSize(13);
        whats.setOnClickListener(v -> openWhatsApp(listing));
        actions.addView(call, new LinearLayout.LayoutParams(0, dp(46), 1));
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(0, dp(46), 1);
        wLp.leftMargin = dp(8);
        actions.addView(whats, wLp);
        box.addView(actions);
        return box;
    }

    private LinearLayout complaintCard(Listing listing) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackground(round(Color.rgb(255, 240, 243), Color.rgb(255, 178, 190), dp(12), 1));
        TextView note = text("Diqqət! Avtomobilə baxış keçirmədən öncə beh göndərməyin.", 12, Color.rgb(165, 30, 50), true);
        box.addView(note);
        TextView report = button("Şikayət et", Color.rgb(250, 220, 239), PURPLE, Color.rgb(232, 187, 220));
        report.setOnClickListener(v -> showReportDialog(listing));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(38));
        rp.topMargin = dp(10);
        box.addView(report, rp);
        return box;
    }


    private String listingKey(Listing listing) {
        if (listing == null) return "";
        if (!TextUtils.isEmpty(listing.id)) return listing.id;
        if (!TextUtils.isEmpty(listing.title)) return listing.title;
        return listing.brand + ":" + listing.model + ":" + listing.year;
    }

    private boolean isFavorite(Listing listing) {
        Boolean value = favoriteState.get(listingKey(listing));
        return value != null && value;
    }

    private void toggleFavorite(Listing listing, TextView heart) {
        if (listing == null) return;
        boolean next = !isFavorite(listing);
        favoriteState.put(listingKey(listing), next);
        updateFavoriteText(heart, next);
        Toast.makeText(this, next ? "Favoritlərə əlavə edildi" : "Favoritlərdən çıxarıldı", Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteText(TextView heart, boolean active) {
        if (heart == null) return;
        heart.setText(active ? "♥" : "♡");
        heart.setTextColor(active ? Color.rgb(255, 31, 60) : Color.rgb(62, 58, 74));
    }

    private TextView galleryNavButton(String symbol) {
        TextView b = text(symbol, 28, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(round(Color.argb(140, 0, 0, 0), Color.TRANSPARENT, dp(18), 0));
        return b;
    }

    private void moveDetailImage(Listing listing, int delta) {
        int count = detailImageCount(listing);
        detailImageIndex = (detailImageIndex + delta + count) % count;
        showDetailInternal(listing, false);
    }

    private int detailImageCount(Listing listing) {
        if (listing != null && listing.imageUris != null && !listing.imageUris.isEmpty()) return Math.max(1, Math.min(15, listing.imageUris.size()));
        return 3;
    }

    private int localDetailImageFor(Listing listing, int index) {
        if (listing == null) return R.drawable.car_01_mercedes_e220;
        if (index <= 0 && listing.localImage != 0) return listing.localImage;
        int[] variants = new int[]{
                R.drawable.car_01_mercedes_e220,
                R.drawable.car_02_bmw_530,
                R.drawable.car_03_hyundai_elantra,
                R.drawable.car_04_toyota_camry,
                R.drawable.car_05_kia_sportage,
                R.drawable.car_06_land_rover,
                R.drawable.car_07_chevrolet_cruze,
                R.drawable.car_08_byd_song
        };
        int hash = (listingKey(listing) + ":" + index).hashCode() & 0x7fffffff;
        return variants[hash % variants.length];
    }

    private void bindDetailImage(Listing listing, ImageView img, int index) {
        if (img == null) return;
        if (listing != null && listing.imageUris != null && index >= 0 && index < listing.imageUris.size()) {
            String value = listing.imageUris.get(index);
            if (isLocalImageUri(value)) {
                try { img.setImageURI(Uri.parse(value)); return; } catch (Exception ignored) { }
            } else if (!TextUtils.isEmpty(value)) {
                loadImage(value, img);
                return;
            }
        }
        if (listing != null && index <= 0 && listing.localImage != 0) {
            img.setImageResource(listing.localImage);
            return;
        }
        if (listing != null && index <= 0 && !TextUtils.isEmpty(listing.imageUrl)) {
            if (isLocalImageUri(listing.imageUrl)) {
                try { img.setImageURI(Uri.parse(listing.imageUrl)); return; } catch (Exception ignored) { }
            }
            loadImage(listing.imageUrl, img);
            return;
        }
        img.setImageResource(localDetailImageFor(listing, index));
    }

    private void updateLightboxImage(Listing listing, ImageView image, TextView counter, int index) {
        bindDetailImage(listing, image, index);
        if (counter != null) counter.setText((index + 1) + "/" + detailImageCount(listing));
    }

    private void showLightbox(Listing listing, int startIndex) {
        int count = detailImageCount(listing);
        final int[] index = new int[]{Math.max(0, Math.min(count - 1, startIndex))};
        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.rgb(8, 7, 12));
        overlay.setOnClickListener(v -> dismissActiveDialog());

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
        imageLp.setMargins(dp(12), dp(86), dp(12), dp(86));
        overlay.addView(image, imageLp);
        image.setOnClickListener(v -> { });

        TextView close = text("×", 28, Color.WHITE, true);
        close.setGravity(Gravity.CENTER);
        close.setBackground(round(Color.argb(120, 255, 255, 255), Color.TRANSPARENT, dp(18), 0));
        close.setOnClickListener(v -> dismissActiveDialog());
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.TOP | Gravity.RIGHT);
        closeLp.setMargins(0, dp(24), dp(18), 0);
        overlay.addView(close, closeLp);

        TextView counter = text("", 13, Color.WHITE, true);
        counter.setGravity(Gravity.CENTER);
        counter.setPadding(dp(12), dp(6), dp(12), dp(6));
        counter.setBackground(round(Color.argb(130, 255, 255, 255), Color.TRANSPARENT, dp(14), 0));
        FrameLayout.LayoutParams counterLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        counterLp.topMargin = dp(32);
        overlay.addView(counter, counterLp);

        TextView prev = galleryNavButton("‹");
        FrameLayout.LayoutParams prevLp = new FrameLayout.LayoutParams(dp(54), dp(70), Gravity.CENTER_VERTICAL | Gravity.LEFT);
        prevLp.leftMargin = dp(12);
        overlay.addView(prev, prevLp);
        TextView next = galleryNavButton("›");
        FrameLayout.LayoutParams nextLp = new FrameLayout.LayoutParams(dp(54), dp(70), Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        nextLp.rightMargin = dp(12);
        overlay.addView(next, nextLp);

        prev.setOnClickListener(v -> { int c = detailImageCount(listing); index[0] = (index[0] + c - 1) % c; updateLightboxImage(listing, image, counter, index[0]); });
        next.setOnClickListener(v -> { int c = detailImageCount(listing); index[0] = (index[0] + 1) % c; updateLightboxImage(listing, image, counter, index[0]); });
        updateLightboxImage(listing, image, counter, index[0]);
        presentDialog(overlay);
    }

    private FrameLayout modalOverlay() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.argb(180, 0, 0, 0));
        overlay.setPadding(dp(16), dp(24), dp(16), dp(24));
        overlay.setOnClickListener(v -> dismissActiveDialog());
        return overlay;
    }

    private LinearLayout modalPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.setBackground(round(Color.WHITE, Color.TRANSPARENT, dp(20), 0));
        panel.setOnClickListener(v -> { });
        return panel;
    }

    private void presentDialog(View view) {
        if (activeDialog != null && activeDialog.isShowing()) activeDialog.dismiss();
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setOnDismissListener(d -> { if (activeDialog == dialog) activeDialog = null; });
        activeDialog = dialog;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void dismissActiveDialog() {
        if (activeDialog != null && activeDialog.isShowing()) activeDialog.dismiss();
        activeDialog = null;
    }

    private void showPriceHistory(Listing listing) {
        FrameLayout overlay = modalOverlay();
        LinearLayout panel = modalPanel();
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView title = text("Qiymət tarixçəsi", 22, TEXT, true);
        panel.addView(title);

        TextView subtitle = text(detailTitle(listing), 13, MUTED, false);
        subtitle.setSingleLine(false);
        subtitle.setMaxLines(2);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        panel.addView(subtitle, topLp(dp(7)));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(6), dp(12), dp(6));
        list.setBackground(round(Color.rgb(248, 246, 252), BORDER, dp(14), 1));
        panel.addView(list, topLp(dp(14)));

        int current = Math.max(0, listing.price);
        addPriceRow(list, "Cari qiymət", current, listing.currency, true);


        TextView close = button("Bağla", PURPLE, Color.WHITE, PURPLE);
        close.setOnClickListener(v -> dismissActiveDialog());
        panel.addView(close, topLp(dp(16)));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        lp.leftMargin = dp(10);
        lp.rightMargin = dp(10);
        overlay.addView(panel, lp);
        presentDialog(overlay);
    }

    private void addPriceRow(LinearLayout parent, String date, int price, String currency, boolean current) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        TextView left = text(date, 13, current ? PURPLE : MUTED, current);
        TextView right = text(formatMoney(price, currency), 14, TEXT, true);
        right.setGravity(Gravity.RIGHT);
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
        parent.addView(row);
    }

    private void showReportDialog(Listing listing) {
        FrameLayout overlay = modalOverlay();
        LinearLayout panel = modalPanel();
        panel.addView(text("Şikayət göndərin", 22, TEXT, true));
        TextView subtitle = text("Səbəbi seçin və göndərin.", 13, MUTED, false);
        subtitle.setLineSpacing(dp(2), 1.05f);
        panel.addView(subtitle, topLp(dp(8)));

        String[] reasons = {"Yanlış məlumat", "Satılıb / aktual deyil", "Şübhəli elan", "Uyğunsuz məzmun", "Digər"};
        final String[] selectedReason = {""};
        final TextView[] reasonViews = new TextView[reasons.length];

        for (int i = 0; i < reasons.length; i++) {
            final int index = i;
            final String reason = reasons[i];
            TextView item = text(reason, 14, TEXT, true);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setMinHeight(dp(48));
            item.setPadding(dp(12), 0, dp(12), 0);
            item.setBackground(round(Color.WHITE, BORDER, dp(12), 1));
            item.setClickable(true);
            item.setFocusable(true);
            item.setOnClickListener(v -> {
                selectedReason[0] = reason;
                for (int j = 0; j < reasonViews.length; j++) {
                    TextView r = reasonViews[j];
                    if (r == null) continue;
                    boolean active = j == index;
                    r.setTextColor(active ? PURPLE : TEXT);
                    r.setBackground(round(active ? Color.rgb(245, 236, 255) : Color.WHITE, active ? PURPLE : BORDER, dp(12), 1));
                }
            });
            reasonViews[i] = item;
            panel.addView(item, topLp(dp(8)));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView cancel = button("Bağla", Color.WHITE, PURPLE, BORDER);
        cancel.setOnClickListener(v -> dismissActiveDialog());
        TextView send = button("Göndər", PURPLE, Color.WHITE, PURPLE);
        send.setOnClickListener(v -> {
            if (TextUtils.isEmpty(selectedReason[0])) {
                Toast.makeText(this, "Əvvəl şikayət səbəbini seçin", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Şikayət göndərildi: " + selectedReason[0], Toast.LENGTH_SHORT).show();
            dismissActiveDialog();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(46), 1));
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(0, dp(46), 1);
        sendLp.leftMargin = dp(8);
        actions.addView(send, sendLp);
        panel.addView(actions, topLp(dp(14)));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        lp.leftMargin = dp(10);
        lp.rightMargin = dp(10);
        overlay.addView(panel, lp);
        presentDialog(overlay);
    }

    private LinearLayout detailStickyBar(Listing listing) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(16), dp(10), dp(16), dp(10));
        bar.setBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 21) bar.setElevation(dp(18));
        TextView call = button("☎ Zəng edin", Color.rgb(20, 190, 112), Color.WHITE, Color.TRANSPARENT);
        call.setTextSize(13);
        call.setOnClickListener(v -> dialPhone(listing));
        TextView whats = button("WhatsApp", PURPLE, Color.WHITE, PURPLE);
        whats.setTextSize(13);
        whats.setOnClickListener(v -> openWhatsApp(listing));
        bar.addView(call, new LinearLayout.LayoutParams(0, dp(52), 1));
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(0, dp(52), 1);
        wLp.leftMargin = dp(12);
        bar.addView(whats, wLp);
        return bar;
    }

    private TextView detailChip(String s, int bg, int fg, int stroke) {
        TextView chip = text(s, 11, fg, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(10), dp(7), dp(10), dp(7));
        chip.setBackground(round(bg, stroke, dp(14), 1));
        return chip;
    }

    private View detailDivider() {
        View v = new View(this);
        v.setBackgroundColor(Color.rgb(237, 234, 243));
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        return v;
    }

    private View specRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(11));
        TextView l = text(label, 12, Color.rgb(130, 128, 142), true);
        TextView v = text(value, 12, Color.rgb(69, 76, 107), true);
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        row.addView(l, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(v, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private TextView smallMeta(String s) {
        TextView v = text(s, 10, MUTED, false);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(8), dp(6), dp(8), dp(6));
        v.setBackground(round(Color.WHITE, BORDER, dp(10), 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = dp(6);
        v.setLayoutParams(lp);
        return v;
    }

    private ArrayList<Listing> relatedRows(Listing current) {
        ArrayList<Listing> out = new ArrayList<>();
        for (Listing row : listings) {
            if (row == current) continue;
            if (!TextUtils.isEmpty(current.brand) && current.brand.equalsIgnoreCase(row.brand)) out.add(row);
        }
        for (Listing row : listings) {
            if (row == current || out.contains(row)) continue;
            out.add(row);
        }
        return out;
    }

    private void bindListingImage(Listing listing, ImageView img) {
        if (listing != null && listing.imageUris != null && !listing.imageUris.isEmpty()) {
            String value = listing.imageUris.get(0);
            if (isLocalImageUri(value)) {
                try { img.setImageURI(Uri.parse(value)); return; } catch (Exception ignored) { }
            } else if (!TextUtils.isEmpty(value)) {
                loadImage(value, img);
                return;
            }
        }
        if (listing != null && listing.localImage != 0) img.setImageResource(listing.localImage);
        else if (listing != null && !TextUtils.isEmpty(listing.imageUrl)) {
            if (isLocalImageUri(listing.imageUrl)) {
                try { img.setImageURI(Uri.parse(listing.imageUrl)); return; } catch (Exception ignored) { }
            }
            loadImage(listing.imageUrl, img);
        }
        else img.setImageResource(R.drawable.car_01_mercedes_e220);
    }

    private boolean isLocalImageUri(String value) {
        return !TextUtils.isEmpty(value) && (value.startsWith("content://") || value.startsWith("file://"));
    }

    private String detailTitle(Listing listing) {
        ArrayList<String> parts = new ArrayList<>();
        if (!TextUtils.isEmpty(listing.brand)) parts.add(listing.brand);
        if (!TextUtils.isEmpty(listing.model)) parts.add(listing.model);
        if (!TextUtils.isEmpty(listing.engine)) parts.add(listing.engine);
        if (!TextUtils.isEmpty(listing.year)) parts.add(listing.year + " il");
        if (!TextUtils.isEmpty(listing.mileage)) parts.add(listing.mileage);
        return TextUtils.isEmpty(TextUtils.join(", ", parts)) ? listing.title : TextUtils.join(", ", parts);
    }

    private String detailEngine(Listing listing) {
        if (!TextUtils.isEmpty(listing.engine) && !TextUtils.isEmpty(listing.horsepower)) return listing.engine + " / " + listing.horsepower + " a.g.";
        return safe(listing.engine, "—");
    }

    private String formatCount(int value) {
        try { return NumberFormat.getIntegerInstance(new Locale("az", "AZ")).format(Math.max(0, value)).replace(',', ' '); }
        catch (Exception ignored) { return String.valueOf(Math.max(0, value)); }
    }

    private String safe(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) return value;
        }
        return "";
    }

    private void dialPhone(Listing listing) {
        if (listing == null) return;
        String phone = normalizeAzPhone(listing.phone);
        if (TextUtils.isEmpty(phone)) phone = String.valueOf(listing.phone == null ? "" : listing.phone).replaceAll("[^+0-9]", "");
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Bu elanda telefon nömrəsi yoxdur", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        } catch (Exception e) {
            Toast.makeText(this, "Zəng tətbiqi açılmadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp(Listing listing) {
        if (listing == null) return;
        String digits = normalizeAzPhone(listing.phone);
        if (TextUtils.isEmpty(digits)) digits = String.valueOf(listing.phone == null ? "" : listing.phone).replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) digits = "994" + digits.substring(1);
        if (TextUtils.isEmpty(digits)) {
            Toast.makeText(this, "Bu elanda WhatsApp nömrəsi yoxdur", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + digits)));
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp açılmadı", Toast.LENGTH_SHORT).show();
        }
    }



    private void seedPromotionServices() {
        promotionServices.clear();
        PromotionService bump = new PromotionService("bump", "Elanı irəli çəkin", "⬆", "Önə çıxma", "Elan siyahıda yuxarı qalxır. Çoxlu paketlərdə 24 saatdan bir avtomatik irəli çəkilir.", "five", Color.rgb(16, 150, 92));
        bump.add("once", "1 dəfə", "Bir dəfə dərhal yuxarı", 3, 1, 1);
        bump.add("three", "3 dəfə", "24 saatdan bir", 6, 3, 3);
        bump.add("five", "5 dəfə", "24 saatdan bir", 9, 5, 5);
        bump.add("ten", "10 dəfə", "24 saatdan bir", 15, 10, 10);
        promotionServices.put("bump", bump);

        PromotionService vip = new PromotionService("vip", "VIP edin", "VIP", "VIP bölməsi", "Elan VIP bölməsində ayrıca görünür və istifadəçinin diqqətini daha tez çəkir.", "d15", Color.rgb(226, 46, 80));
        vip.add("d1", "1 gün", "VIP bölməsində göstərilmə", 5, 1, 0);
        vip.add("d5", "5 gün", "VIP bölməsində göstərilmə", 15, 5, 0);
        vip.add("d15", "15 gün", "VIP bölməsində göstərilmə", 25, 15, 0);
        vip.add("d30", "30 gün", "VIP bölməsində göstərilmə", 40, 30, 0);
        promotionServices.put("vip", vip);

        PromotionService premium = new PromotionService("premium", "Premium edin", "♛", "Premium + VIP", "Premium paket VIP statusu və gündəlik önə çıxmanı birlikdə aktiv edir.", "d15", Color.rgb(190, 132, 0));
        premium.add("d1", "1 gün", "Premium + VIP", 7, 1, 1);
        premium.add("d5", "5 gün", "Hər gün irəli çəkilmə", 20, 5, 5);
        premium.add("d15", "15 gün", "Hər gün irəli çəkilmə", 45, 15, 15);
        premium.add("d30", "30 gün", "Hər gün irəli çəkilmə", 60, 30, 30);
        promotionServices.put("premium", premium);
    }

    private ArrayList<String> billingProductIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (PromotionService service : promotionServices.values()) {
            for (PromotionPlan plan : service.plans) ids.add(plan.productId());
        }
        return ids;
    }

    private void initPlayBilling() {
        try {
            billingClient = BillingClient.newBuilder(this)
                    .setListener(new PurchasesUpdatedListener() {
                        @Override public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                            handlePurchasesUpdated(billingResult, purchases);
                        }
                    })
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                    .enableAutoServiceReconnection()
                    .build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override public void onBillingSetupFinished(BillingResult billingResult) {
                    billingReady = billingResult != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
                    if (billingReady) queryBillingProducts();
                }
                @Override public void onBillingServiceDisconnected() { billingReady = false; }
            });
        } catch (Throwable ignored) {
            billingReady = false;
        }
    }

    private void queryBillingProducts() {
        if (billingClient == null || !billingClient.isReady()) return;
        ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
        for (String id : billingProductIds()) {
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(products).build();
        billingClient.queryProductDetailsAsync(params, (BillingResult result, QueryProductDetailsResult detailsResult) -> {
            if (result == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK || detailsResult == null) return;
            billingProductDetails.clear();
            List<ProductDetails> list = detailsResult.getProductDetailsList();
            if (list == null) return;
            for (ProductDetails pd : list) {
                if (pd != null && !TextUtils.isEmpty(pd.getProductId())) billingProductDetails.put(pd.getProductId(), pd);
            }
        });
    }

    private void fetchPromotionPlans() {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(absoluteApiUrl("/api/promotions/plans"));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(9000);
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("x-byqezi-client", "android-native");
                attachStoredCookies(conn);
                int code = conn.getResponseCode();
                storeResponseCookies(conn, url);
                String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
                if (code < 200 || code >= 300 || TextUtils.isEmpty(json)) return;
                JSONObject obj = new JSONObject(json);
                JSONObject services = obj.optJSONObject("services");
                if (services == null) return;
                for (String serviceKey : promotionServices.keySet()) {
                    PromotionService service = promotionServices.get(serviceKey);
                    JSONObject remoteService = services.optJSONObject(serviceKey);
                    if (service == null || remoteService == null) continue;
                    String remoteTitle = remoteService.optString("title", "");
                    if (!TextUtils.isEmpty(remoteTitle)) service.title = remoteTitle;
                    JSONObject remotePlans = remoteService.optJSONObject("plans");
                    if (remotePlans == null) continue;
                    for (PromotionPlan plan : service.plans) {
                        JSONObject rp = remotePlans.optJSONObject(plan.key);
                        if (rp == null) continue;
                        String label = rp.optString("label", "");
                        if (!TextUtils.isEmpty(label)) plan.label = label;
                        plan.amount = rp.optDouble("amount", plan.amount);
                        plan.days = rp.optInt("days", plan.days);
                        plan.bumps = rp.optInt("bumps", plan.bumps);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private boolean isOwnedListing(Listing listing) {
        if (listing == null) return false;
        if (listing.mine) return true;
        String key = safe(listing.id, "");
        if (TextUtils.isEmpty(key)) return false;
        for (Listing row : userListings) {
            if (key.equals(safe(row.id, ""))) return true;
        }
        return false;
    }

    private LinearLayout promotionQuickRow(Listing listing) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setBackground(round(Color.rgb(253, 250, 255), Color.rgb(224, 210, 242), dp(16), 1));
        TextView copy = text("Elanı önə çıxarın", 14, TEXT, true);
        TextView sub = text("İrəli çək, VIP və Premium paketlər", 11, MUTED, false);
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(copy);
        texts.addView(sub, topLp(dp(6)));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));
        TextView btn = button("Reklam", PURPLE, Color.WHITE, PURPLE);
        btn.setTextSize(13);
        btn.setOnClickListener(v -> showPromotionServices(listing));
        row.addView(btn, new LinearLayout.LayoutParams(dp(104), dp(42)));
        return row;
    }

    private String serviceStartPrice(PromotionService service) {
        double min = 0;
        if (service != null) {
            for (PromotionPlan plan : service.plans) {
                if (plan.amount > 0 && (min <= 0 || plan.amount < min)) min = plan.amount;
            }
        }
        return min > 0 ? moneyLabel(min) + "-dən" : "Seç";
    }

    private void showPromotionServices(Listing listing) {
        if (!userLoggedIn) {
            showLogin("cabinet");
            return;
        }
        if (listing == null) return;
        if (!listing.isActiveStatus()) {
            Toast.makeText(this, "Reklam yalnız Saytda olan aktiv elanlar üçün işləyir", Toast.LENGTH_LONG).show();
            return;
        }
        activeScreen = "promote";
        updateNav();
        LinearLayout panel = modalPanel();
        panel.addView(text("Reklam xidməti", 24, TEXT, true));
        TextView intro = text("Elanınızı daha çox istifadəçiyə göstərmək üçün uyğun paketi seçin.", 13, MUTED, false);
        intro.setLineSpacing(dp(3), 1.05f);
        panel.addView(intro, topLp(dp(10)));
        panel.addView(promotionListingPreview(listing), topLp(dp(14)));
        for (PromotionService service : promotionServices.values()) {
            panel.addView(promotionServiceButton(listing, service), topLp(dp(10)));
        }
        TextView close = button("Bağla", Color.WHITE, TEXT, BORDER);
        close.setOnClickListener(v -> dismissActiveDialog());
        panel.addView(close, topLp(dp(14)));
        presentPromotionPanel(panel);
    }

    private View promotionListingPreview(Listing listing) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackground(round(Color.rgb(248, 248, 251), BORDER, dp(16), 1));
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bindListingImage(listing, img);
        roundClip(img, dp(12));
        box.addView(img, new LinearLayout.LayoutParams(dp(74), dp(58)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        TextView title = text(safe(listing.title, listing.brand + " " + listing.model), 14, TEXT, true);
        title.setMaxLines(2);
        copy.addView(title);
        copy.addView(text(listing.specLine(), 12, MUTED, false), topLp(dp(7)));
        box.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        TextView price = text(formatMoney(listing.price, listing.currency), 13, PURPLE, true);
        box.addView(price);
        return box;
    }

    private View promotionServiceButton(Listing listing, PromotionService service) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(1));
        TextView icon = text(service.icon, service.key.equals("vip") ? 14 : 21, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(circle(service.accent, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(dp(50), dp(50)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(service.title, 16, TEXT, true));
        copy.addView(text(service.note, 12, MUTED, false), topLp(dp(7)));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        TextView price = text(serviceStartPrice(service), 13, PURPLE, true);
        price.setGravity(Gravity.CENTER);
        price.setPadding(dp(9), dp(6), dp(9), dp(6));
        price.setBackground(round(Color.rgb(248, 241, 255), Color.rgb(225, 207, 245), dp(13), 1));
        card.addView(price);
        card.setClickable(true);
        card.setOnClickListener(v -> showPromotionPlans(listing, service.key));
        return card;
    }

    private void showPromotionPlans(Listing listing, String serviceKey) {
        PromotionService service = promotionServices.get(serviceKey);
        if (listing == null || service == null) return;
        activeScreen = "promote";
        updateNav();
        LinearLayout panel = modalPanel();
        TextView back = text("‹ Paketlər", 15, PURPLE, true);
        back.setPadding(0, 0, 0, dp(8));
        back.setOnClickListener(v -> showPromotionServices(listing));
        panel.addView(back);
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = text(service.icon, service.key.equals("vip") ? 14 : 22, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(circle(service.accent, Color.TRANSPARENT, 0));
        titleRow.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout titleCopy = new LinearLayout(this);
        titleCopy.setOrientation(LinearLayout.VERTICAL);
        titleCopy.setPadding(dp(12), 0, 0, 0);
        titleCopy.addView(text(service.title, 22, TEXT, true));
        titleCopy.addView(text(service.note, 12, MUTED, false), topLp(dp(7)));
        titleRow.addView(titleCopy, new LinearLayout.LayoutParams(0, -2, 1));
        panel.addView(titleRow);
        panel.addView(promotionListingPreview(listing), topLp(dp(14)));
        for (PromotionPlan plan : service.plans) {
            panel.addView(promotionPlanCard(listing, service, plan), topLp(dp(10)));
        }
        TextView hint = text("Play Market buildində əsas ödəniş Google Play Billing ilə edilir. Debug/test zamanı məhsullar görünməsə, Epoint fallback ilə real ödənişi yoxlaya bilərsiniz.", 11, Color.rgb(106, 94, 125), false);
        hint.setLineSpacing(dp(3), 1.06f);
        hint.setPadding(dp(12), dp(12), dp(12), dp(12));
        hint.setBackground(round(Color.rgb(250, 247, 255), Color.rgb(224, 210, 242), dp(14), 1));
        panel.addView(hint, topLp(dp(12)));
        presentPromotionPanel(panel);
    }

    private void presentPromotionPanel(LinearLayout panel) {
        FrameLayout overlay = modalOverlay();
        ScrollView sheetScroll = new ScrollView(this);
        sheetScroll.setFillViewport(false);
        sheetScroll.addView(panel, new ScrollView.LayoutParams(-1, -2));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        lp.leftMargin = dp(4);
        lp.rightMargin = dp(4);
        overlay.addView(sheetScroll, lp);
        presentDialog(overlay);
    }

    private View promotionPlanCard(Listing listing, PromotionService service, PromotionPlan plan) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(round(Color.WHITE, BORDER, dp(18), 1));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(plan.displayDuration(), 17, TEXT, true));
        texts.addView(text(plan.hintText(), 12, MUTED, false), topLp(dp(7)));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));
        TextView amount = text(moneyLabel(plan.amount), 16, service.accent, true);
        row.addView(amount);
        card.addView(row);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView play = button("Google Play ilə al", PURPLE, Color.WHITE, PURPLE);
        play.setTextSize(13);
        play.setOnClickListener(v -> startPlayBillingPromotion(listing, service, plan));
        actions.addView(play, new LinearLayout.LayoutParams(0, dp(44), 1));
        TextView epoint = button("Epoint", Color.WHITE, PURPLE, BORDER);
        epoint.setTextSize(13);
        epoint.setOnClickListener(v -> startEpointPromotion(listing, service, plan));
        LinearLayout.LayoutParams eLp = new LinearLayout.LayoutParams(dp(104), dp(44));
        eLp.leftMargin = dp(9);
        actions.addView(epoint, eLp);
        card.addView(actions, topLp(dp(12)));
        return card;
    }

    private void startPlayBillingPromotion(Listing listing, PromotionService service, PromotionPlan plan) {
        if (promotionLoading) return;
        if (listing == null || service == null || plan == null) return;
        if (TextUtils.isEmpty(listing.id)) {
            Toast.makeText(this, "Elan ID tapılmadı", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!billingReady || billingClient == null || !billingClient.isReady()) {
            Toast.makeText(this, "Google Play Billing hələ hazır deyil. Bir az sonra yenidən yoxlayın.", Toast.LENGTH_LONG).show();
            initPlayBilling();
            return;
        }
        ProductDetails details = billingProductDetails.get(plan.productId());
        if (details == null) {
            queryBillingProducts();
            Toast.makeText(this, "Bu məhsul Play Console-da aktiv deyil və ya test track-də görünmür: " + plan.productId(), Toast.LENGTH_LONG).show();
            return;
        }
        pendingPlayPromotion = new PromotionSelection(listing.id, service.key, plan.key, plan.productId());
        BillingFlowParams.ProductDetailsParams productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build();
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productParams))
                .build();
        BillingResult result = billingClient.launchBillingFlow(this, flowParams);
        if (result == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            pendingPlayPromotion = null;
            Toast.makeText(this, "Google Play ödənişi açılmadı", Toast.LENGTH_LONG).show();
        }
    }

    private void handlePurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult == null) return;
        int code = billingResult.getResponseCode();
        if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Ödəniş ləğv edildi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code != BillingClient.BillingResponseCode.OK || purchases == null) {
            Toast.makeText(this, "Ödəniş tamamlanmadı", Toast.LENGTH_LONG).show();
            return;
        }
        for (Purchase purchase : purchases) handlePlayPurchase(purchase);
    }

    private void handlePlayPurchase(Purchase purchase) {
        if (purchase == null) return;
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this, "Ödəniş gözləmədədir. Tamamlananda paket aktivləşəcək.", Toast.LENGTH_LONG).show();
            return;
        }
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;
        PromotionSelection selection = pendingPlayPromotion;
        if (selection == null) {
            selection = selectionFromPurchaseProduct(purchase);
        }
        if (selection == null) {
            Toast.makeText(this, "Ödəniş məhsulu tanınmadı", Toast.LENGTH_LONG).show();
            return;
        }
        confirmGooglePlayPromotion(selection, purchase);
    }

    private PromotionSelection selectionFromPurchaseProduct(Purchase purchase) {
        if (purchase == null || purchase.getProducts() == null || purchase.getProducts().isEmpty()) return null;
        String productId = purchase.getProducts().get(0);
        for (PromotionService service : promotionServices.values()) {
            for (PromotionPlan plan : service.plans) {
                if (plan.productId().equals(productId)) return new PromotionSelection(pendingPromotionListingId, service.key, plan.key, productId);
            }
        }
        return null;
    }

    private void confirmGooglePlayPromotion(PromotionSelection selection, Purchase purchase) {
        if (selection == null || purchase == null || promotionLoading) return;
        promotionLoading = true;
        Toast.makeText(this, "Ödəniş yoxlanılır...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("listing_id", selection.listingId);
                body.put("service", selection.serviceKey);
                body.put("plan", selection.planKey);
                body.put("product_id", selection.productId);
                body.put("purchase_token", purchase.getPurchaseToken());
                body.put("order_id", safe(purchase.getOrderId(), ""));
                body.put("package_name", getPackageName().replace(".debug", ""));
                JSONObject result = postJson("/api/promotions/googleplay/confirm", body);
                main.post(() -> {
                    promotionLoading = false;
                    pendingPlayPromotion = null;
                    consumePlayPurchase(purchase);
                    Toast.makeText(this, result.optString("message", "Reklam paketi aktivləşdirildi"), Toast.LENGTH_LONG).show();
                    dismissActiveDialog();
                    refreshAfterPromotion(selection.listingId);
                });
            } catch (Exception e) {
                main.post(() -> {
                    promotionLoading = false;
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Play Billing təsdiqi alınmadı", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void consumePlayPurchase(Purchase purchase) {
        try {
            if (billingClient == null || purchase == null || TextUtils.isEmpty(purchase.getPurchaseToken())) return;
            ConsumeParams params = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.consumeAsync(params, (billingResult, token) -> { });
        } catch (Exception ignored) { }
    }

    private void startEpointPromotion(Listing listing, PromotionService service, PromotionPlan plan) {
        if (promotionLoading) return;
        if (listing == null || service == null || plan == null) return;
        promotionLoading = true;
        Toast.makeText(this, "Ödəniş yaradılır...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("listing_id", listing.id);
                body.put("listingId", listing.id);
                body.put("service", service.key);
                body.put("plan", plan.key);
                body.put("native", true);
                body.put("prefer_redirect", true);
                JSONObject result = postJson("/api/promotions/create", body);
                String orderId = result.optString("order_id", "");
                String redirect = firstNonEmpty(result.optString("redirect_url", ""), result.optString("payment_url", ""), result.optString("url", ""));
                String checkout = result.optString("checkout_url", "");
                String data = result.optString("data", "");
                String signature = result.optString("signature", "");
                main.post(() -> {
                    promotionLoading = false;
                    if (!TextUtils.isEmpty(orderId)) rememberPendingPromotion(orderId, listing.id);
                    dismissActiveDialog();
                    if (!TextUtils.isEmpty(redirect)) openExternalUrl(redirect);
                    else if (!TextUtils.isEmpty(checkout) && !TextUtils.isEmpty(data) && !TextUtils.isEmpty(signature)) openEpointPostBridge(checkout, data, signature);
                    else Toast.makeText(this, "Ödəniş yönləndirməsi tam deyil", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                main.post(() -> {
                    promotionLoading = false;
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Ödəniş yaradılmadı", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void rememberPendingPromotion(String orderId, String listingId) {
        pendingPromotionOrderId = safe(orderId, "");
        pendingPromotionListingId = safe(listingId, "");
        saveNativeSession();
    }

    private void clearPendingPromotion() {
        pendingPromotionOrderId = "";
        pendingPromotionListingId = "";
        saveNativeSession();
    }

    private void openExternalUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ödəniş səhifəsi açılmadı", Toast.LENGTH_LONG).show();
        }
    }

    private void openEpointPostBridge(String checkoutUrl, String data, String signature) {
        try {
            String html = "<!doctype html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>BYQEZI ödəniş</title></head>"
                    + "<body style='font-family:sans-serif;padding:24px'><h3>BYQEZI ödəniş səhifəsi açılır...</h3>"
                    + "<form id='f' method='post' action='" + htmlEscape(checkoutUrl) + "'>"
                    + "<input type='hidden' name='data' value='" + htmlEscape(data) + "'>"
                    + "<input type='hidden' name='signature' value='" + htmlEscape(signature) + "'>"
                    + "<button type='submit'>Davam et</button></form><script>document.getElementById('f').submit()</script></body></html>";
            String encoded = Base64.encodeToString(html.getBytes("UTF-8"), Base64.NO_WRAP);
            openExternalUrl("data:text/html;base64," + encoded);
        } catch (Exception e) {
            Toast.makeText(this, "Epoint checkout açıla bilmədi", Toast.LENGTH_LONG).show();
        }
    }

    private String htmlEscape(String value) {
        return String.valueOf(value == null ? "" : value)
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void checkPromotionStatus(String orderId, boolean silent) {
        if (TextUtils.isEmpty(orderId)) return;
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                ensureCsrfCookie();
                URL url = new URL(absoluteApiUrl("/api/promotions/status?order_id=" + Uri.encode(orderId)));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("accept", "application/json");
                attachCsrfHeader(conn);
                int code = conn.getResponseCode();
                storeResponseCookies(conn, url);
                String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
                JSONObject obj = TextUtils.isEmpty(json) ? new JSONObject() : new JSONObject(json);
                if (code < 200 || code >= 300 || !obj.optBoolean("ok", false)) return;
                JSONObject payment = obj.optJSONObject("payment");
                String status = payment == null ? "" : payment.optString("status", "");
                if ("success".equalsIgnoreCase(status)) {
                    final String promotedListingId = pendingPromotionListingId;
                    main.post(() -> {
                        clearPendingPromotion();
                        if (!silent) Toast.makeText(this, "Reklam paketi aktivləşdirildi", Toast.LENGTH_LONG).show();
                        refreshAfterPromotion(promotedListingId);
                    });
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void refreshAfterPromotion(String listingId) {
        refreshUserListings(true);
        fetchListings();
        if ("cabinet".equals(activeScreen)) showCabinet();
    }

    private String moneyLabel(double amount) {
        try {
            return String.format(Locale.US, "%.2f AZN", amount).replace(".", ",");
        } catch (Exception ignored) {
            return amount + " AZN";
        }
    }

    private void syncPromoRowsFromListings() {
        premiumListings.clear();
        vipListings.clear();
        for (Listing row : listings) {
            if (row.isPremiumActive()) premiumListings.add(row);
            if (row.isVipActive()) vipListings.add(row);
        }
    }

    private ArrayList<Listing> premiumRows() {
        ArrayList<Listing> source = premiumListings.isEmpty() ? listings : premiumListings;
        ArrayList<Listing> out = new ArrayList<>();
        for (Listing row : source) if (row.isPremiumActive() && matchesCurrentSearch(row)) out.add(row);
        return out;
    }

    private ArrayList<Listing> vipRows() {
        ArrayList<Listing> source = vipListings.isEmpty() ? listings : vipListings;
        ArrayList<Listing> out = new ArrayList<>();
        for (Listing row : source) if (row.isVipActive() && matchesCurrentSearch(row)) out.add(row);
        return out;
    }

    private boolean matchesCurrentSearch(Listing row) {
        if (row == null) return false;
        String b = brandQuery.trim().toLowerCase(Locale.ROOT);
        String m = modelQuery.trim().toLowerCase(Locale.ROOT);
        String brand = String.valueOf(row.brand == null ? "" : row.brand).toLowerCase(Locale.ROOT);
        String model = String.valueOf(row.model == null ? "" : row.model).toLowerCase(Locale.ROOT);
        if (!b.isEmpty() && !brand.contains(b)) return false;
        if (!m.isEmpty() && !model.contains(m)) return false;

        String city = filterCity.trim().toLowerCase(Locale.ROOT);
        String rowCity = String.valueOf(row.city == null ? "" : row.city).toLowerCase(Locale.ROOT);
        if (!city.isEmpty() && !rowCity.contains(city)) return false;

        int minPrice = cleanInt(filterMinPrice);
        int maxPrice = cleanInt(filterMaxPrice);
        if (minPrice > 0 && row.price < minPrice) return false;
        if (maxPrice > 0 && row.price > maxPrice) return false;

        int rowYear = cleanInt(row.year);
        int minYear = cleanInt(filterMinYear);
        int maxYear = cleanInt(filterMaxYear);
        if (minYear > 0 && (rowYear <= 0 || rowYear < minYear)) return false;
        if (maxYear > 0 && (rowYear <= 0 || rowYear > maxYear)) return false;

        if (!TextUtils.isEmpty(filterCondition) && !containsNormalized(row.condition, filterCondition)) return false;
        if (!TextUtils.isEmpty(filterBodyType) && !containsNormalized(row.bodyType, filterBodyType)) return false;
        if (!TextUtils.isEmpty(filterCurrency) && !"AZN".equalsIgnoreCase(filterCurrency) && !containsNormalized(row.currency, filterCurrency)) return false;

        int rowMileage = cleanInt(row.mileage);
        int minMileage = cleanInt(filterMinMileage);
        int maxMileage = cleanInt(filterMaxMileage);
        if (minMileage > 0 && (rowMileage <= 0 || rowMileage < minMileage)) return false;
        if (maxMileage > 0 && (rowMileage <= 0 || rowMileage > maxMileage)) return false;

        double rowEngine = cleanDouble(row.engine);
        double minEngine = cleanDouble(filterMinEngine);
        double maxEngine = cleanDouble(filterMaxEngine);
        if (minEngine > 0 && (rowEngine <= 0 || rowEngine < minEngine)) return false;
        if (maxEngine > 0 && (rowEngine <= 0 || rowEngine > maxEngine)) return false;

        int rowPower = cleanInt(row.horsepower);
        int minPower = cleanInt(filterMinPower);
        int maxPower = cleanInt(filterMaxPower);
        if (minPower > 0 && (rowPower <= 0 || rowPower < minPower)) return false;
        if (maxPower > 0 && (rowPower <= 0 || rowPower > maxPower)) return false;

        if (!TextUtils.isEmpty(filterColor) && !containsNormalized(row.color, filterColor)) return false;
        if (!TextUtils.isEmpty(filterFuel) && !containsNormalized(row.fuel, filterFuel)) return false;
        if (!TextUtils.isEmpty(filterDrivetrain) && !containsNormalized(row.drivetrain, filterDrivetrain)) return false;
        if (!TextUtils.isEmpty(filterTransmission) && !containsNormalized(row.transmission, filterTransmission)) return false;
        if (!TextUtils.isEmpty(filterSeats) && !String.valueOf(row.seats == null ? "" : row.seats).trim().equalsIgnoreCase(filterSeats)) return false;
        if (!TextUtils.isEmpty(filterOwners)) {
            String owners = String.valueOf(row.ownersCount == null ? "" : row.ownersCount).trim();
            if ("4 və daha çox".equals(filterOwners)) {
                int ownersInt = cleanInt(owners);
                if (ownersInt > 0 && ownersInt < 4) return false;
                if (ownersInt <= 0 && !containsNormalized(owners, filterOwners)) return false;
            } else if (!owners.equalsIgnoreCase(filterOwners)) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(filterMarket) && !containsNormalized(row.market, filterMarket)) return false;
        if (!TextUtils.isEmpty(filterSaleType) && !containsNormalized(row.saleType, filterSaleType)) return false;

        for (String item : filterEquipment) {
            boolean matched = false;
            for (String eq : row.equipment) {
                if (containsNormalized(eq, item)) { matched = true; break; }
            }
            if (!matched) return false;
        }
        for (String state : filterStates) {
            if ("Vuruğu yoxdur".equals(state) && !containsNormalized(row.accidentPaint, "Vuruğu yoxdur")) return false;
            if ("Rənglənməyib".equals(state) && !containsNormalized(row.accidentPaint, "Rənglənməyib")) return false;
            if ("Yalnız qəzalı avtomobillər".equals(state) && !containsNormalized(row.accidentPaint, "Qəzalı")) return false;
            if ("Kredit".equals(state) && !row.credit) return false;
            if ("Barter".equals(state) && !row.barter) return false;
            if ("Yalnız şəkilli".equals(state) && row.imageUris.isEmpty() && TextUtils.isEmpty(row.imageUrl) && row.localImage == 0) return false;
        }
        return true;
    }

    private ArrayList<Listing> regularRows() {
        ArrayList<Listing> out = new ArrayList<>();
        for (Listing row : listings) {
            if (row.isPremiumActive() || row.isVipActive()) continue;
            if (!matchesCurrentSearch(row)) continue;
            out.add(row);
        }
        return out;
    }

    private ArrayList<Listing> visibleRows(boolean vipOnly) {
        ArrayList<Listing> out = new ArrayList<>();
        for (Listing row : listings) {
            if (vipOnly && !row.isVipActive()) continue;
            if (!matchesCurrentSearch(row)) continue;
            out.add(row);
        }
        return out;
    }

    private void fetchListings() {
        loadingRemote = true;
        executor.execute(() -> {
            ArrayList<Listing> remote = requestListings("/api/listings?limit=100");
            ArrayList<Listing> remotePremium = requestListings("/api/listings?collection=premium&limit=100");
            ArrayList<Listing> remoteVip = requestListings("/api/listings?collection=vip&limit=100");
            main.post(() -> {
                loadingRemote = false;
                if (!remote.isEmpty()) {
                    listings.clear();
                    listings.addAll(remote);
                    premiumListings.clear();
                    premiumListings.addAll(remotePremium);
                    vipListings.clear();
                    vipListings.addAll(remoteVip);
                    if (premiumListings.isEmpty()) {
                        for (Listing row : listings) if (row.isPremiumActive()) premiumListings.add(row);
                    }
                    if (vipListings.isEmpty()) {
                        for (Listing row : listings) if (row.isVipActive()) vipListings.add(row);
                    }
                    usingLocalFallback = false;
                    if ("home".equals(activeScreen)) showHome(false);
                } else {
                    listings.clear();
                    premiumListings.clear();
                    vipListings.clear();
                    usingLocalFallback = false;
                    if ("home".equals(activeScreen)) showHome(false);
                }
            });
        });
    }

    private JSONArray listingsArrayFrom(JSONObject obj) {
        if (obj == null) return null;
        JSONArray arr = obj.optJSONArray("listings");
        if (arr != null) return arr;
        arr = obj.optJSONArray("items");
        if (arr != null) return arr;
        arr = obj.optJSONArray("results");
        if (arr != null) return arr;
        arr = obj.optJSONArray("data");
        if (arr != null) return arr;
        JSONObject data = obj.optJSONObject("data");
        if (data != null) {
            arr = data.optJSONArray("listings");
            if (arr != null) return arr;
            arr = data.optJSONArray("items");
            if (arr != null) return arr;
            arr = data.optJSONArray("results");
            if (arr != null) return arr;
        }
        return null;
    }

    private ArrayList<Listing> requestListings(String pathAndQuery) {
        ArrayList<Listing> rows = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(absoluteApiUrl(pathAndQuery));
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(9000);
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("x-byqezi-client", "android-native");
            attachStoredCookies(conn);
            int code = conn.getResponseCode();
            storeResponseCookies(conn, url);
            InputStream raw = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            String json = readAll(raw);
            if (code < 200 || code >= 300 || TextUtils.isEmpty(json)) return rows;
            JSONObject obj = new JSONObject(json);
            JSONArray arr = listingsArrayFrom(obj);
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    if (item != null) rows.add(Listing.fromJson(item, i));
                }
            }
            saveNativeSession();
        } catch (Exception ignored) {
            rows.clear();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return rows;
    }

    private ArrayList<Listing> requestUserListings() {
        // requestListings("/api/listings?mine=1&limit=100") remains the primary cabinet fetch path via requestUserListings().
        String[] paths = new String[] {
                "/api/listings?mine=1&limit=100",
                "/api/listings?mine=1&status=all&limit=100",
                "/api/listings?mine=1&include_pending=1&limit=100",
                "/api/listings?owner=me&limit=100",
                "/api/listings?scope=mine&limit=100",
                "/api/my/listings?limit=100",
                "/api/user/listings?limit=100",
                "/api/account/listings?limit=100"
        };
        for (String path : paths) {
            ArrayList<Listing> rows = requestListings(path);
            if (!rows.isEmpty()) {
                for (Listing row : rows) row.mine = true;
                return rows;
            }
        }

        ArrayList<Listing> fallback = new ArrayList<>();
        String myPhone = normalizeAzPhone(userPhone);
        if (!TextUtils.isEmpty(myPhone)) {
            ArrayList<Listing> publicRows = requestListings("/api/listings?limit=100");
            for (Listing row : publicRows) {
                if (row != null && myPhone.equals(normalizeAzPhone(row.phone))) {
                    row.mine = true;
                    fallback.add(row);
                }
            }
        }
        return fallback;
    }

    private Listing requestListingDetail(String id) throws Exception {
        if (TextUtils.isEmpty(id)) return null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(absoluteApiUrl("/api/listings/" + Uri.encode(id)));
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("x-byqezi-client", "android-native");
            attachStoredCookies(conn);
            int code = conn.getResponseCode();
            storeResponseCookies(conn, url);
            String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
            JSONObject obj = TextUtils.isEmpty(json) ? new JSONObject() : new JSONObject(json);
            if (code < 200 || code >= 300 || !obj.optBoolean("ok", true)) {
                throw new Exception(obj.optString("error", "Elan məlumatı yüklənmədi"));
            }
            JSONObject item = obj.optJSONObject("listing");
            if (item == null) item = obj.optJSONObject("item");
            if (item == null) item = obj.optJSONObject("data");
            if (item == null) return null;
            Listing row = Listing.fromJson(item, 0);
            saveNativeSession();
            return row;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private boolean isCsrfCookieName(String name) {
        String n = String.valueOf(name == null ? "" : name).trim().toLowerCase(Locale.ROOT);
        return "byqezi_csrf".equals(n)
                || "csrf".equals(n)
                || "csrf_token".equals(n)
                || "xsrf-token".equals(n)
                || "xsrftoken".equals(n)
                || n.contains("csrf")
                || n.contains("xsrf");
    }

    private String storedCookieHeader() {
        ArrayList<String> parts = new ArrayList<>();
        ArrayList<String> seen = new ArrayList<>();
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return "";
            List<HttpCookie> cookies = nativeCookieManager.getCookieStore().getCookies();
            if (cookies == null) return "";
            for (HttpCookie cookie : cookies) {
                if (cookie == null || cookie.hasExpired()) continue;
                String name = safe(cookie.getName(), "");
                String value = safe(cookie.getValue(), "");
                if (TextUtils.isEmpty(name) || seen.contains(name.toLowerCase(Locale.ROOT))) continue;
                seen.add(name.toLowerCase(Locale.ROOT));
                parts.add(name + "=" + value);
            }
        } catch (Exception ignored) { }
        return TextUtils.join("; ", parts);
    }

    private void attachStoredCookies(HttpURLConnection conn) {
        if (conn == null) return;
        String cookieHeader = storedCookieHeader();
        if (!TextUtils.isEmpty(cookieHeader)) conn.setRequestProperty("Cookie", cookieHeader);
    }

    private List<HttpCookie> parseResponseCookies(String header) {
        ArrayList<HttpCookie> out = new ArrayList<>();
        if (TextUtils.isEmpty(header)) return out;
        try {
            out.addAll(HttpCookie.parse(header));
            return out;
        } catch (Exception ignored) { }
        try {
            String first = header.split(";", 2)[0];
            int eq = first.indexOf('=');
            if (eq > 0) out.add(new HttpCookie(first.substring(0, eq).trim(), first.substring(eq + 1).trim()));
        } catch (Exception ignored) { }
        return out;
    }

    private void storeResponseCookies(HttpURLConnection conn, URL url) {
        try {
            if (conn == null || url == null || nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return;
            Map<String, List<String>> headers = conn.getHeaderFields();
            if (headers == null) return;
            URI uri = url.toURI();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (key == null || !("set-cookie".equalsIgnoreCase(key) || "set-cookie2".equalsIgnoreCase(key))) continue;
                List<String> values = entry.getValue();
                if (values == null) continue;
                for (String value : values) {
                    for (HttpCookie cookie : parseResponseCookies(value)) {
                        if (cookie == null || TextUtils.isEmpty(cookie.getName())) continue;
                        if (TextUtils.isEmpty(cookie.getPath())) cookie.setPath("/");
                        nativeCookieManager.getCookieStore().add(uri, cookie);
                    }
                }
            }
            saveNativeSession();
        } catch (Exception ignored) { }
    }

    private void applyManualCsrfToken(String token) {
        token = String.valueOf(token == null ? "" : token).trim();
        if (TextUtils.isEmpty(token)) return;
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return;
            HttpCookie cookie = new HttpCookie("byqezi_csrf", token);
            cookie.setPath("/");
            cookie.setSecure(BuildConfig.API_BASE_URL.toLowerCase(Locale.ROOT).startsWith("https://"));
            cookie.setMaxAge(3600);
            nativeCookieManager.getCookieStore().add(URI.create(BuildConfig.API_BASE_URL), cookie);
            saveNativeSession();
        } catch (Exception ignored) { }
    }

    private void clearCsrfCookies() {
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return;
            URI baseUri = URI.create(BuildConfig.API_BASE_URL);
            ArrayList<HttpCookie> remove = new ArrayList<>();
            List<HttpCookie> cookies = nativeCookieManager.getCookieStore().getCookies();
            if (cookies != null) {
                for (HttpCookie cookie : cookies) if (cookie != null && isCsrfCookieName(cookie.getName())) remove.add(cookie);
            }
            for (HttpCookie cookie : remove) {
                try { nativeCookieManager.getCookieStore().remove(baseUri, cookie); } catch (Exception ignored) { }
                try { nativeCookieManager.getCookieStore().remove(null, cookie); } catch (Exception ignored) { }
            }
            saveNativeSession();
        } catch (Exception ignored) { }
    }

    private boolean isSecurityTokenError(int code, String message) {
        String m = String.valueOf(message == null ? "" : message).toLowerCase(Locale.ROOT);
        return code == 401 || code == 403 || code == 419
                || m.contains("csrf")
                || m.contains("xsrf")
                || m.contains("token")
                || m.contains("təhlükəsizlik")
                || m.contains("tehlukesizlik");
    }

    private String csrfTokenFromCookies() {
        try {
            if (nativeCookieManager == null || nativeCookieManager.getCookieStore() == null) return "";
            List<HttpCookie> cookies = nativeCookieManager.getCookieStore().getCookies();
            if (cookies == null) return "";
            for (HttpCookie cookie : cookies) {
                if (cookie != null && !cookie.hasExpired() && isCsrfCookieName(cookie.getName())) {
                    return String.valueOf(cookie.getValue() == null ? "" : cookie.getValue()).trim();
                }
            }
        } catch (Exception ignored) { }
        return "";
    }

    private void attachCsrfHeader(HttpURLConnection conn) {
        if (conn == null) return;
        attachStoredCookies(conn);
        conn.setRequestProperty("x-byqezi-client", "android-native");
        conn.setRequestProperty("origin", BuildConfig.API_BASE_URL);
        conn.setRequestProperty("referer", BuildConfig.API_BASE_URL + "/");
        String token = csrfTokenFromCookies();
        if (!TextUtils.isEmpty(token)) {
            conn.setRequestProperty("x-csrf-token", token);
            conn.setRequestProperty("x-byqezi-csrf", token);
            conn.setRequestProperty("x-xsrf-token", token);
            conn.setRequestProperty("csrf-token", token);
        }
    }

    private void ensureCsrfCookie() {
        if (!TextUtils.isEmpty(csrfTokenFromCookies())) return;
        String[] paths = new String[] { "/api/security/turnstile", "/api/security/csrf", "/api/csrf" };
        for (String path : paths) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(absoluteApiUrl(path));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(9000);
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("x-byqezi-client", "android-native");
                attachStoredCookies(conn);
                int code = conn.getResponseCode();
                storeResponseCookies(conn, url);
                String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
                if (code >= 200 && code < 300 && !TextUtils.isEmpty(json)) {
                    JSONObject obj = new JSONObject(json);
                    String token = firstNonEmpty(
                            obj.optString("csrf", ""),
                            obj.optString("csrfToken", ""),
                            obj.optString("csrf_token", ""),
                            obj.optString("xsrfToken", ""),
                            obj.optString("token", "")
                    );
                    JSONObject data = obj.optJSONObject("data");
                    if (TextUtils.isEmpty(token) && data != null) {
                        token = firstNonEmpty(
                                data.optString("csrf", ""),
                                data.optString("csrfToken", ""),
                                data.optString("csrf_token", ""),
                                data.optString("xsrfToken", ""),
                                data.optString("token", "")
                        );
                    }
                    applyManualCsrfToken(token);
                }
                saveNativeSession();
                if (!TextUtils.isEmpty(csrfTokenFromCookies())) return;
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
    }

    private JSONObject postJson(String path, JSONObject body) throws Exception {
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            HttpURLConnection conn = null;
            try {
                if (attempt > 0) clearCsrfCookies();
                ensureCsrfCookie();
                URL url = new URL(absoluteApiUrl(path));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(9000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("content-type", "application/json; charset=utf-8");
                attachCsrfHeader(conn);
                byte[] bytes = (body == null ? new JSONObject() : body).toString().getBytes("UTF-8");
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
                int code = conn.getResponseCode();
                storeResponseCookies(conn, url);
                String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
                JSONObject obj = TextUtils.isEmpty(json) ? new JSONObject() : new JSONObject(json);
                if (code < 200 || code >= 300 || !obj.optBoolean("ok", true)) {
                    String error = obj.optString("error", "Server xətası: " + code);
                    if (attempt == 0 && isSecurityTokenError(code, error)) {
                        last = new Exception(error);
                        continue;
                    }
                    throw new Exception(error);
                }
                saveNativeSession();
                return obj;
            } catch (Exception e) {
                last = e;
                if (attempt == 0 && isSecurityTokenError(0, e.getMessage())) continue;
                throw e;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw last == null ? new Exception("Server xətası") : last;
    }

    private ArrayList<String> uploadListingImages(ArrayList<String> uriStrings) throws Exception {
        if (uriStrings == null || uriStrings.isEmpty()) return new ArrayList<>();
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            ArrayList<String> urls = new ArrayList<>();
            HttpURLConnection conn = null;
            String boundary = "----BYQEZIAndroid" + System.currentTimeMillis() + "_" + attempt;
            try {
                if (attempt > 0) clearCsrfCookies();
                ensureCsrfCookie();
                URL url = new URL(absoluteApiUrl("/api/upload"));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(60000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
                attachCsrfHeader(conn);
                OutputStream out = conn.getOutputStream();
                int count = Math.min(uriStrings.size(), MAX_ADD_IMAGES);
                for (int i = 0; i < count; i++) {
                    Uri uri = Uri.parse(uriStrings.get(i));
                    String mime = getContentResolver().getType(uri);
                    if (TextUtils.isEmpty(mime)) mime = "image/jpeg";
                    String ext = "jpg";
                    if ("image/png".equalsIgnoreCase(mime)) ext = "png";
                    else if ("image/webp".equalsIgnoreCase(mime)) ext = "webp";
                    else if (!"image/jpeg".equalsIgnoreCase(mime)) mime = "image/jpeg";
                    writeUtf8(out, "--" + boundary + "\r\n");
                    writeUtf8(out, "Content-Disposition: form-data; name=\"images\"; filename=\"byqezi_" + (i + 1) + "." + ext + "\"\r\n");
                    writeUtf8(out, "Content-Type: " + mime + "\r\n\r\n");
                    InputStream in = getContentResolver().openInputStream(uri);
                    if (in == null) throw new Exception("Şəkil oxunmadı");
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                    in.close();
                    writeUtf8(out, "\r\n");
                }
                writeUtf8(out, "--" + boundary + "--\r\n");
                out.flush();
                out.close();
                int code = conn.getResponseCode();
                storeResponseCookies(conn, url);
                String json = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
                JSONObject obj = TextUtils.isEmpty(json) ? new JSONObject() : new JSONObject(json);
                if (code < 200 || code >= 300 || !obj.optBoolean("ok", false)) {
                    String error = obj.optString("error", "Şəkil yüklənmədi: " + code);
                    if (attempt == 0 && isSecurityTokenError(code, error)) {
                        last = new Exception(error);
                        continue;
                    }
                    throw new Exception(error);
                }
                JSONArray arr = obj.optJSONArray("urls");
                if (arr == null) arr = obj.optJSONArray("images");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        String item = arr.optString(i, "");
                        JSONObject itemObj = arr.optJSONObject(i);
                        if (TextUtils.isEmpty(item) && itemObj != null) item = firstNonEmpty(itemObj.optString("url", ""), itemObj.optString("src", ""));
                        if (!TextUtils.isEmpty(item)) urls.add(item);
                    }
                }
                if (urls.size() < MIN_ADD_IMAGES) throw new Exception("Şəkillər serverə tam yüklənmədi");
                saveNativeSession();
                return urls;
            } catch (Exception e) {
                last = e;
                if (attempt == 0 && isSecurityTokenError(0, e.getMessage())) continue;
                throw e;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw last == null ? new Exception("Şəkillər serverə tam yüklənmədi") : last;
    }

    private void writeUtf8(OutputStream out, String value) throws Exception {
        out.write(String.valueOf(value == null ? "" : value).getBytes("UTF-8"));
    }

    private String absoluteApiUrl(String pathOrUrl) {
        String raw = String.valueOf(pathOrUrl == null ? "" : pathOrUrl).trim();
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw;
        if (!raw.startsWith("/")) raw = "/" + raw;
        return BuildConfig.API_BASE_URL + raw;
    }

    private void loadImage(String url, ImageView target) {
        final String absolute = absoluteApiUrl(url);
        Bitmap cached = imageCache.get(absolute);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(absolute).openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(9000);
                InputStream in = new BufferedInputStream(conn.getInputStream());
                Bitmap bmp = BitmapFactory.decodeStream(in);
                if (bmp != null) {
                    imageCache.put(absolute, bmp);
                    main.post(() -> target.setImageBitmap(bmp));
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }


    private void setModelEnabled(boolean enabled, String brand) {
        if (currentModelInput == null) return;
        currentModelInput.setEnabled(enabled);
        currentModelInput.setAlpha(enabled ? 1f : 0.75f);
        currentModelInput.setHint(enabled ? "Model seçin və ya yazın" : "Əvvəl marka seçin");
        setAdapter(currentModelInput, enabled ? modelsFor(brand) : new String[0]);
    }

    private void maybeShowBrandDropdown() {
        if (currentBrandInput == null) return;
        String query = currentBrandInput.getText() == null ? "" : currentBrandInput.getText().toString().trim();
        if (!TextUtils.isEmpty(exactBrand(query))) {
            currentBrandInput.dismissDropDown();
            return;
        }
        String[] options = filteredBrands(query);
        setAdapter(currentBrandInput, options);
        if (options.length > 0) currentBrandInput.showDropDown();
        else currentBrandInput.dismissDropDown();
    }

    private String[] filteredBrands(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        ArrayList<String> out = new ArrayList<>();
        if (needle.isEmpty()) {
            for (int i = 0; i < Math.min(7, BRANDS.length); i++) out.add(BRANDS[i]);
            return out.toArray(new String[0]);
        }
        for (String b : BRANDS) if (b.toLowerCase(Locale.ROOT).startsWith(needle)) out.add(b);
        for (String b : BRANDS) {
            String lower = b.toLowerCase(Locale.ROOT);
            if (!lower.startsWith(needle) && lower.contains(needle)) out.add(b);
        }
        return out.toArray(new String[0]);
    }

    private String[] modelsFor(String brand) {
        if (TextUtils.isEmpty(brand)) return new String[0];
        String exact = exactBrand(brand);
        if (TextUtils.isEmpty(exact)) return new String[0];
        for (Map.Entry<String, String[]> entry : MODELS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(exact)) return entry.getValue();
        }
        return new String[0];
    }

    private String[] modelsForBrand(String brand) {
        return modelsFor(brand);
    }

    private String exactBrand(String value) {
        String query = String.valueOf(value == null ? "" : value).trim();
        if (query.isEmpty()) return "";
        for (String brand : BRANDS) {
            if (brand.equalsIgnoreCase(query)) return brand;
        }
        return "";
    }

    private AutoCompleteTextView autoComplete(String hint, String[] options) {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setTextSize(18);
        input.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        input.setTextColor(TEXT);
        input.setHintTextColor(LIGHT_MUTED);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setThreshold(0);
        input.setPadding(dp(18), 0, dp(18), 0);
        input.setBackground(round(FIELD, BORDER, dp(18), 1));
        input.setDropDownBackgroundDrawable(round(Color.WHITE, BORDER, dp(14), 1));
        setAdapter(input, options);
        input.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus && ((AutoCompleteTextView)v).isEnabled()) ((AutoCompleteTextView)v).showDropDown(); });
        input.setOnClickListener(v -> { if (((AutoCompleteTextView)v).isEnabled()) ((AutoCompleteTextView)v).showDropDown(); });
        return input;
    }

    private void setAdapter(AutoCompleteTextView input, String[] options) {
        if (input == null) return;
        try {
            String[] safe = options == null ? new String[0] : options;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, safe);
            input.setAdapter(adapter);
        } catch (Exception ignored) {
            // Adapter changes must never crash the app during brand/model selection.
        }
    }

    private LinearLayout.LayoutParams inputLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.topMargin = dp(8);
        return lp;
    }

    private TextView label(String s) {
        TextView l = text(s, 14, Color.rgb(81, 80, 98), true);
        l.setGravity(Gravity.LEFT);
        return l;
    }

    private TextView button(String s, int bg, int fg, int stroke) {
        TextView b = text(s, 16, fg, true);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(42));
        b.setPadding(dp(12), dp(8), dp(12), dp(8));
        b.setBackground(round(bg, stroke, dp(18), 1));
        return b;
    }

    private TextView tinyBadge(String text, int bg, int fg) {
        TextView v = text(text, 8, fg, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(6), dp(3), dp(6), dp(3));
        v.setBackground(round(bg, Color.TRANSPARENT, dp(10), 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = dp(4);
        v.setLayoutParams(lp);
        return v;
    }

    private ImageView localImage(int res) {
        ImageView img = new ImageView(this);
        img.setImageResource(res);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setAdjustViewBounds(false);
        img.setBackground(round(Color.WHITE, BORDER, dp(16), 1));
        roundClip(img, dp(16));
        return img;
    }

    private LinearLayout.LayoutParams topLp(int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = top;
        return lp;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private GradientDrawable round(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(radius);
        if (strokeWidth > 0) g.setStroke(dp(strokeWidth), stroke);
        return g;
    }

    private GradientDrawable circle(int fill, int stroke, int strokeWidth) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        if (strokeWidth > 0) g.setStroke(dp(strokeWidth), stroke);
        return g;
    }

    private void roundClip(View view, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setClipToOutline(true);
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View v, Outline outline) {
                    outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius);
                }
            });
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private String formatMoney(int amount, String currency) {
        try {
            NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("az", "AZ"));
            return nf.format(amount).replace(',', ' ') + " " + (TextUtils.isEmpty(currency) ? "AZN" : currency);
        } catch (Exception e) {
            return amount + " " + (TextUtils.isEmpty(currency) ? "AZN" : currency);
        }
    }

    private String readAll(InputStream in) throws Exception {
        if (in == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private interface ChoiceHandler { void onChoice(String value); }
    private interface ValueHandler { void onValue(String value); }


    private static class PromotionService {
        final String key;
        String title;
        final String icon;
        final String shortLabel;
        final String note;
        final String defaultPlan;
        final int accent;
        final ArrayList<PromotionPlan> plans = new ArrayList<>();
        PromotionService(String key, String title, String icon, String shortLabel, String note, String defaultPlan, int accent) {
            this.key = key;
            this.title = title;
            this.icon = icon;
            this.shortLabel = shortLabel;
            this.note = note;
            this.defaultPlan = defaultPlan;
            this.accent = accent;
        }
        void add(String key, String label, String hint, double amount, int days, int bumps) {
            plans.add(new PromotionPlan(this.key, key, label, hint, amount, days, bumps));
        }
    }

    private static class PromotionPlan {
        final String serviceKey;
        final String key;
        String label;
        String hint;
        double amount;
        int days;
        int bumps;
        PromotionPlan(String serviceKey, String key, String label, String hint, double amount, int days, int bumps) {
            this.serviceKey = serviceKey;
            this.key = key;
            this.label = label;
            this.hint = hint;
            this.amount = amount;
            this.days = days;
            this.bumps = bumps;
        }
        String productId() { return serviceKey + "_" + key; }
        String displayDuration() {
            String clean = String.valueOf(label == null ? "" : label).replaceAll("\\s*/.*$", "").trim();
            return TextUtils.isEmpty(clean) ? key : clean;
        }
        String hintText() {
            if (!TextUtils.isEmpty(hint)) return hint;
            if (bumps > 1) return bumps + " dəfə irəli çəkilmə";
            if (days > 0) return days + " gün avtomatik aktivləşmə";
            return "Avtomatik aktivləşmə";
        }
    }

    private static class PromotionSelection {
        final String listingId;
        final String serviceKey;
        final String planKey;
        final String productId;
        PromotionSelection(String listingId, String serviceKey, String planKey, String productId) {
            this.listingId = listingId;
            this.serviceKey = serviceKey;
            this.planKey = planKey;
            this.productId = productId;
        }
    }

    private static class AddListingDraft {
        String vehicleCategory = "Minik";
        String brand = "";
        String model = "";
        String year = "";
        String bodyType = "";
        String fuel = "Benzin";
        String drivetrain = "";
        String transmission = "";
        String engineVolume = "";
        String horsepower = "";
        String mileage = "";
        String seats = "5";
        String color = "";
        String market = "Avropa";
        String modification = "";
        String ownersCount = "1";
        String condition = "Sürülmüş";
        String city = "Bakı";
        String price = "";
        String currency = "AZN";
        String saleType = "Satışdadır";
        String sellerName = "";
        String phone = "";
        String description = "";
        String vinCode = "";
        boolean credit = false;
        boolean barter = false;
        boolean noAccident = true;
        boolean notRepainted = true;
        boolean damaged = false;
        boolean vin = false;
        boolean showExtraTech = false;
        final ArrayList<String> imageUris = new ArrayList<>();
        final ArrayList<String> equipment = new ArrayList<>();

        AddListingDraft copy() {
            AddListingDraft d = new AddListingDraft();
            d.vehicleCategory = vehicleCategory;
            d.brand = brand;
            d.model = model;
            d.year = year;
            d.bodyType = bodyType;
            d.fuel = fuel;
            d.drivetrain = drivetrain;
            d.transmission = transmission;
            d.engineVolume = engineVolume;
            d.horsepower = horsepower;
            d.mileage = mileage;
            d.seats = seats;
            d.color = color;
            d.market = market;
            d.modification = modification;
            d.ownersCount = ownersCount;
            d.condition = condition;
            d.city = city;
            d.price = price;
            d.currency = currency;
            d.saleType = saleType;
            d.sellerName = sellerName;
            d.phone = phone;
            d.description = description;
            d.vinCode = vinCode;
            d.credit = credit;
            d.barter = barter;
            d.noAccident = noAccident;
            d.notRepainted = notRepainted;
            d.damaged = damaged;
            d.vin = vin;
            d.showExtraTech = showExtraTech;
            d.imageUris.addAll(imageUris);
            d.equipment.addAll(equipment);
            return d;
        }

        void addImage(String uri) {
            if (TextUtils.isEmpty(uri) || imageUris.contains(uri) || imageUris.size() >= MAX_ADD_IMAGES) return;
            imageUris.add(uri);
        }
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    private static class Listing {
        String title;
        String brand;
        String model;
        String year;
        String engine;
        String fuel;
        String mileage;
        String city;
        String time;
        String currency;
        String imageUrl;
        final ArrayList<String> imageUris = new ArrayList<>();
        final ArrayList<String> equipment = new ArrayList<>();
        int localImage;
        int price;
        int views;
        boolean vip;
        boolean premium;
        boolean credit;
        boolean barter;
        String vipUntil;
        String premiumUntil;
        String id;
        String phone;
        String sellerName;
        String bodyType;
        String color;
        String transmission;
        String drivetrain;
        String condition;
        String market;
        String seats;
        String ownersCount;
        String saleType;
        String accidentPaint;
        String description;
        String horsepower;
        String status;
        String createdAt;
        String updatedAt;
        boolean mine;

        private static String firstNonEmpty(String... values) {
            if (values == null) return "";
            for (String value : values) {
                if (!TextUtils.isEmpty(value)) return value;
            }
            return "";
        }

        private static boolean boolValue(JSONObject obj, String... keys) {
            if (obj == null || keys == null) return false;
            for (String key : keys) {
                if (!obj.has(key)) continue;
                Object value = obj.opt(key);
                if (value instanceof Boolean) return (Boolean) value;
                if (value instanceof Number) return ((Number) value).intValue() != 0;
                String text = String.valueOf(value == null ? "" : value).trim().toLowerCase(Locale.ROOT);
                if ("1".equals(text) || "true".equals(text) || "yes".equals(text) || "on".equals(text)) return true;
                if ("0".equals(text) || "false".equals(text) || "no".equals(text) || "off".equals(text)) return false;
            }
            return false;
        }

        private static int intValue(JSONObject obj, String... keys) {
            if (obj == null || keys == null) return 0;
            for (String key : keys) {
                if (!obj.has(key)) continue;
                Object value = obj.opt(key);
                if (value instanceof Number) return Math.max(0, ((Number) value).intValue());
                try {
                    String digits = String.valueOf(value == null ? "" : value).replaceAll("[^0-9]", "");
                    if (!TextUtils.isEmpty(digits)) return Math.max(0, Integer.parseInt(digits));
                } catch (Exception ignored) { }
            }
            return 0;
        }

        private static String normalizedEngine(String engineVolume, String engine, String fuel) {
            String volume = String.valueOf(engineVolume == null ? "" : engineVolume).trim();
            if (!TextUtils.isEmpty(volume)) {
                volume = volume.replace(',', '.').replaceAll("[^0-9.]", "");
                if (volume.endsWith(".")) volume = volume.substring(0, volume.length() - 1);
                if (!TextUtils.isEmpty(volume)) return volume + " L";
            }
            if (!TextUtils.isEmpty(engine)) return engine;
            return TextUtils.isEmpty(fuel) ? "" : fuel;
        }

        private static String normalizedMileage(String value) {
            String text = String.valueOf(value == null ? "" : value).trim();
            if (TextUtils.isEmpty(text)) return "";
            if (text.toLowerCase(Locale.ROOT).contains("km")) return text;
            try {
                int n = Integer.parseInt(text.replaceAll("[^0-9]", ""));
                if (n <= 0 && !"0".equals(text)) return "";
                return NumberFormat.getIntegerInstance(new Locale("az", "AZ")).format(n).replace(',', ' ') + " km";
            } catch (Exception ignored) {
                return text;
            }
        }

        private static void addArrayItems(ArrayList<String> target, JSONArray arr) {
            if (target == null || arr == null) return;
            for (int i = 0; i < arr.length(); i++) {
                String value = arr.optString(i, "").trim();
                if (!TextUtils.isEmpty(value) && !target.contains(value)) target.add(value);
            }
        }

        private static void addJsonArrayStringItems(ArrayList<String> target, String raw) {
            if (target == null || TextUtils.isEmpty(raw)) return;
            try {
                addArrayItems(target, new JSONArray(raw));
            } catch (Exception ignored) {
                String[] parts = raw.split("\\n|,");
                for (String part : parts) {
                    String value = part == null ? "" : part.trim();
                    if (!TextUtils.isEmpty(value) && !target.contains(value)) target.add(value);
                }
            }
        }

        private static long parseDateMs(String value) {
            if (TextUtils.isEmpty(value)) return 0L;
            String text = String.valueOf(value).trim();
            if (TextUtils.isEmpty(text)) return 0L;
            String normalized = text.contains("T") ? text : text.replace(" ", "T");
            if (!normalized.matches(".*(?:Z|[+-]\\d{2}:?\\d{2})$")) normalized = normalized + "Z";
            normalized = normalized.replace("Z", "+0000").replaceAll("([+-]\\d{2}):(\\d{2})$", "$1$2");
            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    "yyyy-MM-dd'T'HH:mm:ssZ",
                    "yyyy-MM-dd'T'HH:mmZ",
                    "yyyy-MM-ddZ"
            };
            for (String pattern : patterns) {
                try {
                    Date d = new SimpleDateFormat(pattern, Locale.US).parse(normalized);
                    if (d != null) return d.getTime();
                } catch (Exception ignored) { }
            }
            return 0L;
        }

        private static String relativeTime(String value) {
            long ms = parseDateMs(value);
            if (ms <= 0L) return "";
            long diff = Math.max(0L, System.currentTimeMillis() - ms);
            long minute = 60_000L;
            long hour = 60L * minute;
            long day = 24L * hour;
            if (diff < minute) return "indi";
            if (diff < hour) return (diff / minute) + " dəq əvvəl";
            if (diff < day) return (diff / hour) + " saat əvvəl";
            if (diff < 2L * day) return "dünən";
            if (diff < 7L * day) return (diff / day) + " gün əvvəl";
            try {
                return new SimpleDateFormat("dd.MM.yyyy", new Locale("az", "AZ")).format(new Date(ms));
            } catch (Exception ignored) {
                return "";
            }
        }

        String fullDateText() {
            long ms = parseDateMs(firstNonEmpty(createdAt, updatedAt));
            if (ms <= 0L) return "—";
            try {
                return new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("az", "AZ")).format(new Date(ms));
            } catch (Exception ignored) {
                return firstNonEmpty(createdAt, updatedAt, "—");
            }
        }

        String specLine() {
            ArrayList<String> parts = new ArrayList<>();
            if (!TextUtils.isEmpty(year)) parts.add(year);
            if (!TextUtils.isEmpty(engine)) parts.add(engine);
            if (!TextUtils.isEmpty(mileage)) parts.add(mileage);
            return TextUtils.join(" • ", parts);
        }

        boolean isVipActive() { return vip && futureOrEmpty(vipUntil); }
        boolean isPremiumActive() { return premium && futureOrEmpty(premiumUntil); }
        boolean isPendingStatus() {
            String s = String.valueOf(status == null ? "" : status).trim().toLowerCase(Locale.US);
            return "pending".equals(s) || "new".equals(s) || "review".equals(s) || "reviewing".equals(s) || "waiting".equals(s) || "gözləmədə".equals(s);
        }
        boolean isRejectedStatus() {
            String s = String.valueOf(status == null ? "" : status).trim().toLowerCase(Locale.US);
            return "rejected".equals(s) || "declined".equals(s) || "refused".equals(s);
        }
        boolean isActiveStatus() {
            String s = String.valueOf(status == null ? "" : status).trim().toLowerCase(Locale.US);
            return TextUtils.isEmpty(s) || "active".equals(s) || "published".equals(s) || "approved".equals(s) || "saytda".equals(s);
        }

        private static boolean futureOrEmpty(String value) {
            if (TextUtils.isEmpty(value)) return true;
            long ms = parseDateMs(value);
            return ms <= 0L || ms > System.currentTimeMillis();
        }

        static Listing fromJson(JSONObject obj, int index) {
            Listing l = new Listing();
            l.currency = "AZN";
            l.status = "active";
            l.id = String.valueOf(index + 1);
            if (obj == null) {
                l.title = "Avtomobil elanı";
                return l;
            }
            l.brand = obj.optString("brand", "").trim();
            l.model = obj.optString("model", "").trim();
            l.title = firstNonEmpty(obj.optString("title", ""), (l.brand + " " + l.model).trim(), "Avtomobil elanı");
            l.price = intValue(obj, "price");
            l.currency = firstNonEmpty(obj.optString("currency", ""), "AZN");
            l.year = obj.optString("year", "").trim();
            l.fuel = obj.optString("fuel", "").trim();
            l.engine = normalizedEngine(obj.optString("engine_volume", ""), obj.optString("engine", ""), l.fuel);
            l.mileage = normalizedMileage(obj.optString("mileage", ""));
            l.city = obj.optString("city", "").trim();
            l.createdAt = firstNonEmpty(obj.optString("created_at", ""), obj.optString("createdAt", ""), obj.optString("created", ""));
            l.updatedAt = firstNonEmpty(obj.optString("updated_at", ""), obj.optString("updatedAt", ""), obj.optString("updated", ""));
            l.time = relativeTime(firstNonEmpty(l.createdAt, l.updatedAt));
            l.views = intValue(obj, "views", "view_count", "views_count");
            l.vip = boolValue(obj, "vip", "is_vip");
            l.premium = boolValue(obj, "featured", "premium", "is_premium", "is_featured");
            l.credit = boolValue(obj, "credit");
            l.barter = boolValue(obj, "barter");
            l.vipUntil = firstNonEmpty(obj.optString("vip_until", ""), obj.optString("vip_expires_at", ""));
            l.premiumUntil = firstNonEmpty(obj.optString("premium_until", ""), obj.optString("featured_until", ""), obj.optString("premium_expires_at", ""));
            l.id = obj.optString("id", String.valueOf(index + 1));
            l.status = firstNonEmpty(obj.optString("status", ""), obj.optString("state", ""), obj.optString("moderation_status", ""), "active");
            l.mine = boolValue(obj, "mine", "is_mine");
            l.sellerName = firstNonEmpty(obj.optString("seller_name", ""), obj.optString("owner_name", ""), obj.optString("user_name", ""), "Satıcı");
            l.phone = firstNonEmpty(obj.optString("phone", ""), obj.optString("whatsapp", ""));
            l.bodyType = firstNonEmpty(obj.optString("body_type", ""), obj.optString("body", ""));
            l.color = obj.optString("color", "").trim();
            l.transmission = obj.optString("transmission", "").trim();
            l.drivetrain = obj.optString("drivetrain", "").trim();
            l.condition = obj.optString("condition", "").trim();
            l.market = obj.optString("market", "").trim();
            l.seats = obj.optString("seats", "").trim();
            l.ownersCount = firstNonEmpty(obj.optString("owners_count", ""), obj.optString("owners", ""));
            l.saleType = firstNonEmpty(obj.optString("sale_type", ""), obj.optString("saleType", ""));
            ArrayList<String> accident = new ArrayList<>();
            if (boolValue(obj, "damaged")) accident.add("Qəzalı / hissə üçün");
            if (boolValue(obj, "no_accident")) accident.add("Vuruğu yoxdur");
            if (boolValue(obj, "not_repainted")) accident.add("Rənglənməyib");
            l.accidentPaint = firstNonEmpty(TextUtils.join(", ", accident), obj.optString("accident_paint", ""));
            l.description = obj.optString("description", "").trim();
            l.horsepower = obj.optString("horsepower", "").trim();
            addArrayItems(l.equipment, obj.optJSONArray("equipment"));
            addJsonArrayStringItems(l.equipment, obj.optString("equipment_json", ""));
            JSONArray images = obj.optJSONArray("images");
            addArrayItems(l.imageUris, images);
            if (!l.imageUris.isEmpty()) l.imageUrl = l.imageUris.get(0);
            if (TextUtils.isEmpty(l.imageUrl)) {
                addJsonArrayStringItems(l.imageUris, obj.optString("images_json", ""));
                if (!l.imageUris.isEmpty()) l.imageUrl = l.imageUris.get(0);
            }
            int[] fallback = {R.drawable.car_02_bmw_530, R.drawable.car_06_land_rover, R.drawable.car_01_mercedes_e220, R.drawable.car_03_hyundai_elantra, R.drawable.car_04_toyota_camry, R.drawable.car_05_kia_sportage};
            l.localImage = fallback[Math.abs(index) % fallback.length];
            return l;
        }
    }

}
