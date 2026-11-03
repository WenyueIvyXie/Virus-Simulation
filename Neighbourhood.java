/** Stores a square's current state and its remaining infected/vaccinated cycles. */
public class Neighbourhood {
    Status status;
    int timer;

    public Neighbourhood(Status status, int timer){
        this.status = status;
        this.timer = timer;
    }
    public Neighbourhood(){
        this.status = Status.SUSCEPTIBLE;
        this.timer = 0;
    }
}
