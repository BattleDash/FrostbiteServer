package me.battledash.kyber.engine.simulation.input;

public class EntryInputActionBindings {

    public static long NumNetworkedAnalogInputs = 8;
    public static long NumAnalogInputs = 10;
    public static long FirstDigital = 10;
    public static long NumNetworkedDigitalInputs = 54;
    public static long NumDigitalInputs = 60;
    public static long NumInputs = 70;
    public static long SignedAnalogInputs = 1019;

    public static long MaxNetworkedAnalogInputs = 32;
    public static long MaxNetworkedDigitalInputs = 64;
    public static long MaxNetworkedInputs = EntryInputActionBindings.MaxNetworkedAnalogInputs +
            EntryInputActionBindings.MaxNetworkedDigitalInputs;
    public static long MaxAnalogInputs = EntryInputActionBindings.MaxNetworkedAnalogInputs + 0;
    public static long MaxDigitalInputs = EntryInputActionBindings.MaxNetworkedDigitalInputs + 0;
    public static long MaxInputs = EntryInputActionBindings.MaxAnalogInputs + EntryInputActionBindings.MaxDigitalInputs;

    public static boolean isSignedAnalogForIndex(long actionIndex) {
        return actionIndex < EntryInputActionBindings.SignedAnalogInputs && ((EntryInputActionBindings.SignedAnalogInputs & 1L << actionIndex) != 0);
    }

}
