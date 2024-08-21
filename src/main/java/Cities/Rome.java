package Cities;
public class Rome {
    int points;

    public Rome() {
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

    public String Get1() {
        return "img/rome1.jpg";
    }

    public String Get2() {
        return "img/rome2.jpg";
    }

    public String Get3() {
        return "img/rome3.jpg";
    }

    public String Get4() {
        return "img/rome4.jpg";
    }
}

