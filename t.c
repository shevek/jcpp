#define var(x...) a x __VA_ARGS__ b
var(b, c, d)

#define var2(x, ...) a x __VA_ARGS__ b
var2(b, c, d)

#define var3(...) a x __VA_ARGS__ b
var3(b, c, d)
