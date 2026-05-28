# Home Round 4 Compile Fix

GitHub Actions failed at `:app:compileDebugJavaWithJavac` because the Java regex string used `\d` as a single Java escape inside a string literal.

Fixed lines:

```java
replaceAll("([+-]\\\\d{2}):(\\\\d{2})$", "$1$2")
```

This keeps the regex digit matcher as `\d` at runtime while compiling correctly in Java source.

No UI changes were made in this package. It only fixes compilation and updates the Round 4 validator to catch this case.
