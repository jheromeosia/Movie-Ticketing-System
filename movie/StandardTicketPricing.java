package movie;

//Standard Ticket Pricing (Inherits TicketPricing)
public final class StandardTicketPricing extends TicketPricing {
    //Getting of the Zero Discount Rate
    @Override
    protected double getDiscountRate() {
        return 0.0;
    }
}