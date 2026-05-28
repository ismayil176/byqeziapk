# Add Listing Round 1 Compile Fix

GitHub build failed because `updateAddModelForBrand()` called `modelsForBrand(exact)`, but the helper method was missing.

Fixed by adding:

```java
private String[] modelsForBrand(String brand) {
    return modelsFor(brand);
}
```

No UI or feature logic was changed.
