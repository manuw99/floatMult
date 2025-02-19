public class Main {

    public static void displayBits(int num) {
        int bit;
        for (bit = (Integer.SIZE - 1); bit >= 0; bit--) {
            if (bit == 22) {
                System.out.print("| ");
            }
            if (bit == 30) {
                System.out.print("| ");
            }

            System.out.print(((num >> bit) & 0x01) + " ");
        }
        System.out.println();
    }

    public static void displayBitsFloat(float f)
    {
        int result = Float.floatToIntBits(f);
        displayBits(result);
    }

    public static int floatToIEEE754(float f)
    {
        int result = Float.floatToIntBits(f);
        return result;
    }

    public static float IEEE754ToFloat(int i)
    {
        float result = Float.intBitsToFloat(i);
        return result;
    }

    public static enum RoundingMode {ROUND_TO_NEAREST, ROUND_TOWARD_ZERO, ROUND_TOWARD_POS_INF, ROUND_TOWARD_NEG_INF};

    public static int roundToNearest(int ir) {
        int stickyBit = 0;
        int ir_copy = ir;

        // Perform a right shift on a copy of ir, setting the sticky bit if any non-zero bits are shifted out
        for (int i = 0; i < 23; i++) {
            stickyBit |= ir_copy & 1;
            ir_copy >>= 1;
        }

        // Round to the nearest even number
        if ((ir & 0x1) != 0 && (stickyBit != 0 || (ir & 0x2) != 0)) {
            System.out.println("Rounding up");
            return ir + 1;
        }
        return ir;
    }

    public static int roundTowardZero(int ir) {
        int mantissa = ir & 0x007FFFFF; // Extract the mantissa
        int exponent = ir & 0x7F800000; // Extract the exponent
        int sign = ir & 0x80000000; // Extract the sign bit

        // For normalized numbers
        if (exponent != 0 && exponent != 0x7F800000) {
            // Mask the mantissa to truncate to an integer
            int shift = 23 - ((exponent >> 23) - 127); // Calculate how much we need to shift to get the integer part
            if (shift > 0) {
                mantissa >>= shift; // Right shift to truncate
                mantissa <<= shift; // Left shift back to original position
            }
        }
        // For subnormal numbers or zero, the mantissa is already rounded towards zero
        // For NaNs and infinities, we don't modify the mantissa

        return sign | exponent | mantissa; // Recombine the components
    }


    public static int roundTowardPosInf(int ir) {
        int stickyBit = 0;
        int ir_copy = ir;

        // Perform a right shift on a copy of ir, setting the sticky bit if any non-zero bits are shifted out
        for (int i = 0; i < 23; i++) {
            stickyBit |= ir_copy & 1;
            ir_copy >>= 1;
        }

        if (stickyBit != 0) { // If fractional part exists, then increment ir.
            return ir + 1;
        }
        return ir;
    }


    public static int roundTowardNegInf(int ir) {
        int stickyBit = 0;
        int ir_copy = ir;

        // Perform a right shift on a copy of ir, setting the sticky bit if any non-zero bits are shifted out
        for (int i = 0; i < 23; i++) {
            stickyBit |= ir_copy & 1;
            ir_copy >>= 1;
        }

        if (stickyBit != 0) { // If fractional part exists, then increment ir.
            return ir + 1;
        }
        return ir;
    }

    public static int roundResult(int ir, RoundingMode mode) {
        switch (mode) {
            case ROUND_TO_NEAREST:
                return roundToNearest(ir);
            case ROUND_TOWARD_ZERO:
                return roundTowardZero(ir);
            case ROUND_TOWARD_POS_INF:
                return roundTowardPosInf(ir);
            case ROUND_TOWARD_NEG_INF:
                return roundTowardNegInf(ir);
            default:
                return ir; // Default case should not be reached
        }
    }

    public static float multiply(float a, float b, RoundingMode mode){

        int x = floatToIEEE754(a);
        int y = floatToIEEE754(b);




        // EXCEPTIONS

        // If either input is NaN, return qNaN
        if ((x & 0x7F800000) == 0x7F800000 && (x & 0x007FFFFF) != 0 ||
                (y & 0x7F800000) == 0x7F800000 && (y & 0x007FFFFF) != 0)
        {
            // Return qNaN in this case
            System.out.println("NaN");
            return IEEE754ToFloat(0x7FC00000);
        }

        // If either input is 0, return 0
        if ((x & 0x7F800000) == 0 || (y & 0x7F800000) == 0)
        {
            // If either input is 0 AND the other input is infinity return qNaN
            if ((x & 0x7F800000) == 0x7F800000 || (y & 0x7F800000) == 0x7F800000){
                System.out.println("NaN");
                return IEEE754ToFloat(0x7FC00000);
            }
            else{
                System.out.println("0");
                return IEEE754ToFloat(0);
            }
        }

        // If either input is infinity
        if (((x & 0x7F800000) == 0x7F800000) || ((y & 0x7F800000) == 0x7F800000))
        {
            // Return Infinity in this case
            System.out.println("Infinity");
            return Float.POSITIVE_INFINITY;
        }

        // If both inputs are infinities and of the same sign return +Infinity
        if (((x & 0x7F800000) == 0x7F800000) && ((y & 0x7F800000) == 0x7F800000) && ((x==y)))
        {
            // Return Infinity
            System.out.println("+Infinity");
            return Float.POSITIVE_INFINITY;
        }

        // If both inputs are infinities return -Infinity
        if (((x & 0x7F800000) == 0x7F800000) && ((y & 0x7F800000) == 0x7F800000))
        {
            // Return -Infinity in this case
            System.out.println("-Infinity");
            return Float.NEGATIVE_INFINITY;
        }



        // Check if either input is subnormal
        boolean isAsubnormal = ((x & 0x7F800000) == 0) && ((x & 0x007FFFFF) != 0);
        boolean isBsubnormal = ((y & 0x7F800000) == 0) && ((y & 0x007FFFFF) != 0);

        //Mantissa and Sign extraction of first number (x)
        int signa = (x >> 31) & 0x01;
        int mana = (x & 0x007FFFFF) | (isAsubnormal ? 0 : 0x00800000);

        System.out.println("Sign bit a    :  "   + signa);
        displayBits(signa);
        System.out.println("Mantissa a    :  "   + mana);
        displayBits(mana);

        //this exponent extraction was removed and replaced by unbiased exponent extraction further below
 /*       int expa = x & 0x7F800000;
        System.out.println("Exponent A   :  " + expa);
        displayBits(expa);
        */

        //Mantissa and Sign extraction of second number (y)
        int signb = (y >> 31) & 0x01;
        int manb = (y & 0x007FFFFF) | (isBsubnormal ? 0 : 0x00800000);

        System.out.println("Sign bit b    :  "   + signb);
        displayBits(signb);
        System.out.println("Mantissa b    :  "   + manb);
        displayBits(manb);


        //this exponent extraction was removed and replaced by unbiased exponent extraction further below
  /*      int expb = y & 0x7F800000;
        System.out.println("Exponent B   :  "   + expb);
        displayBits(expb);
        */


        // calculating sign bit, signa^signb suffices for this
        int sign = signa ^ signb;

        System.out.println("Sign bit  result  :  "   + sign);
        displayBits(sign);


        //this if / else statement was unnecessary
        /*
        if(signa == signb){
            sign = 0 & 0x80000000;
            System.out.println("Positives Ergebnis!");
        }
        else{
            sign = 1 | 0x80000000;
            System.out.println("Negatives Ergebnis!");
        }*/








        // calculating exponent

        // unbiased exponent of first number calculation
        int expa_unbiased = ((x >> 23) & 0xff);

        System.out.println("Ergebnis Exponent 1   : "  +expa_unbiased);
        displayBits(expa_unbiased);

        // unbiased exponent of second number calculation
        int expb_unbiased = ((y >> 23) & 0xff);

        System.out.println("Ergebnis Exponent 2  : "  +expb_unbiased);
        displayBits(expb_unbiased);

        // exponent calculation, re-adding the bias
        int exponent = (expa_unbiased + expb_unbiased - 127);

        System.out.println("Ergebnis Exponent + bias  : "  +exponent);
        displayBits(exponent);


        // Check for overflow (all exponent bits are 1)
        if (exponent == 0x7F800000)
        {
            System.out.println("Exponent Overflow!");
            return IEEE754ToFloat(sign | (exponent & 0xFF800000));
        }

        // Check for exponent overflow (>254)
        if (exponent > 0xFE){
            System.out.println("Exponent Overflow! 1");
            return IEEE754ToFloat(sign | (exponent & 0xFF800000));
        }

        // Check for exponent underflow (<-126)
        if (exponent < -126){
            System.out.println("Exponent Underflow! 2");
            return IEEE754ToFloat(0);
        }


        //calculating mantissa, temporarily storing in long to not overflow the int

        long mantissa;

        mantissa = (((long)mana*(long)manb)>>23)&0x0000000001fffffff;

        if (((mantissa >> 24) & 1) == 1) {
            exponent = exponent + 1;
            mantissa = mantissa >> 1;
        }

        int mantissafinal = (int)mantissa;


        // Build result and return it

        int result = (sign<<31) | ((exponent<<23)&0x7F800000) | (mantissafinal&0x007FFFFF);
        result = roundResult(result, mode);
        return IEEE754ToFloat(result);





/*
        int exponent = (((expa + expb) - 127) >> 23) & 0xFF;
        System.out.println("Exponent:    " + exponent);
        displayBits(exponent);




        int mresult = sign | (exponent & 0x7F800000) | (mant &  0x007FFFFF);
        System.out.println(mresult);
        mresult = roundResult(mresult, mode);
        return IEEE754ToFloat(mresult);
 */



    }


    public static void main(String[] args) {
      /*  displayBits(12345);
        displayBitsFloat(3.14f);
        System.out.println(Float.floatToIntBits(3.14f));
        System.out.println(roundToNearest(9));
        roundTowardZero(19999);
        int a = floatToIEEE754(0.127e12f);
        System.out.println(a);
        int b = floatToIEEE754(0.373e12f);
        System.out.println(b);
        */

        RoundingMode mode = RoundingMode.ROUND_TO_NEAREST;
        displayBits(-1);
        float a = 3.5f;
        float b = 2.25f;
        System.out.println("Zahl 1  : " +a);
        System.out.println("Zahl 2  : " +b);


        float result1 = multiply(a,b, mode);
        System.out.println("Ergebnis Multiplikation  1 :  " +result1);
        displayBitsFloat(result1);

        System.out.println();
        float testres1 = a*b;
        System.out.println("Test result  1 :   "   +testres1);
        displayBitsFloat(testres1);

    }


}