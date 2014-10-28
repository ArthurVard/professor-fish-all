#include<stdio.h>

int factorial(int n) {
  int v = 1; // next label 
  int r = 1;
  while (1) {
    switch (v) {
      case 1: // start
	if (n == 0)
	  v = 2;
	else {
	  r = r * n;
	  n = n - 1;
	  v = 1;
	}
	break;
      case 2: // end
	return r;
    }
  }
}

int main() {
  printf("%i\n", factorial(5));
  return 0;
}
