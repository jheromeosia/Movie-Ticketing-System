package movie;

//Shared Ticket Price Calculations
public abstract class TicketPricing {
    private static final double VAT_RATE = 0.12;

    //Getting of the Discount Rate (Set by Child Classes)
    protected abstract double getDiscountRate();

    //Getting of the Discount as a Whole Percent
    public int getDiscountPercent() {
        return (int) Math.round(getDiscountRate() * 100.0);
    }

    //Computing of the Discount Amount
    public double calculateDiscount(double subtotal) {
        return roundToCents(subtotal * getDiscountRate());
    }

    //Computing of the VAT Inside the Price
    public double calculateVat(double subtotal) {
        double amountAfterDiscount = subtotal - calculateDiscount(subtotal);
        return roundToCents(amountAfterDiscount * VAT_RATE);
    }

    //Computing of the Final Total
    public double calculateTotal(double subtotal) {
        double amountAfterDiscount = subtotal - calculateDiscount(subtotal);
        return roundToCents(amountAfterDiscount);
    }

    //Computing of the Price Without VAT
    public double calculatePriceBeforeVat(double subtotal) {
        return roundToCents(calculateTotal(subtotal) - calculateVat(subtotal));
    }

    //Rounding of Money to Two Decimals
    private double roundToCents(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}