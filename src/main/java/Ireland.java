public class Ireland {
    int points;

    public Ireland() {
        this.points = 0;
    }

    void Gotfirst() {
        this.points += 5;
    }

    void Gotsecond() {
        this.points += 4;
    }

    void Gotthird() {
        this.points += 3;
    }

    void Gotforth() {
        this.points += 1;
    }

    int getPoints() {
        return points;
    }

    String Get1() {
        return "img/ireland1.jpg";
    }

    String Get2() {
        return "img/ireland2.jpg";
    }

    String Get3() {
        return "img/ireland3.jpg";
    }

    String Get4() {
        return "img/ireland4.jpg";
    }
}

