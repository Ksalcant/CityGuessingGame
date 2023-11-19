public class Phoenix {
    int points;

    public Phoenix() {
        this.points =0;
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
        return "img/phoenix1.jpg";
    }

    String Get2() {
        return "img/phoenix2.jpg";
    }

    String Get3() {
        return "img/phoenix3.jpg";
    }

    String Get4() {
        return "img/phoenix4.jpg";
    }
}

