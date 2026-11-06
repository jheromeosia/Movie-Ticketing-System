package movie;

//Priority Ticket Pricing (Inherits TicketPricing)
public final class PriorityTicketPricing extends TicketPricing {
    private double discountRate;

    //Limiting of the Discount Rate (0% to 50%)
    public PriorityTicketPricing(double rate) {
        if (rate < 0.0 || rate > 0.50) {
            rate = 0.0;
        }
        discountRate = rate;
    }

    //Getting of the Priority Discount Rate
    @Override
    protected double getDiscountRate() {
        return discountRate;
    }
}