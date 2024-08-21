package Cities;
public class Ireland {
    int points;

    public Ireland() {
        this.points = 0;
    }

    public  void Gotfirst() {
        this.points += 5;
    }

    public void Gotsecond() {
        this.points += 4;
    }

    public void Gotthird() {
        this.points += 3;
    }

    public void Gotforth() {
        this.points += 1;
    }

    public int getPoints() {
        return points;
    }

    public String Get1() {
        return "img/ireland1.jpg";
    }

    public String Get2() {
        return "img/ireland2.jpg";
    }

    public String Get3() {
        return "img/ireland3.jpg";
    }

    public String Get4() {
        return "img/ireland4.jpg";
    }
}

