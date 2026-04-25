int main() {
    int x = 5;
    int y = 2;
    x = x + y * 3;
    if (x > 0) {
        x--;
    } else {
        x = 0;
    }
    printf("x=%d", x);
    return x;
}