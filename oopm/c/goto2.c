#include<stdio.h>

int factorial(int n) {
  int r = 1;
start:
  if (n!=0) {
    r = r * n;
    n = n - 1;
    goto start;
  }
  return r;
}

int main() {
  printf("%i\n", factorial(5));
  return 0;
}
