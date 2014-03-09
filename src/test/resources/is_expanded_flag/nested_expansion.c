#define DIVISIBLE2(n) n % 2 == 0
#define DIVISIBLE3(n) n % 3 == 0
#define DIVISIBLE6(n) DIVISIBLE2(n) && DIVISIBLE3(n)

bool isDivisible = DIVISIBLE6(3423);
