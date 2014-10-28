#include<stdio.h>

int factorial(int n) {
  int r = 1;
  while (n!=0) {
    r = r * n;
    n = n - 1;
  }
  return r;
}

int main() {
  printf("%i\n", factorial(5));
  return 0;
}
