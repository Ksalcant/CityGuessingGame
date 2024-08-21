package Cities;
public class Phoenix {
    int points;

    public Phoenix() {
        this.points =0;
    }

    public void  Gotfirst() {
        this.points += 5;
    }

    public void  Gotsecond() {
        this.points += 4;
    }

    public void  Gotthird() {
        this.points += 3;
    }

    public void  Gotforth() {
        this.points += 1;
    }

    public int getPoints() {
        return points;
    }

    public String   Get1() {
        return "img/phoenix1.jpg";
    }

    public String   Get2() {
        return "img/phoenix2.jpg";
    }

    public String   Get3() {
        return "img/phoenix3.jpg";
    }

    public String   Get4() {
        return "img/phoenix4.jpg";
    }
}

