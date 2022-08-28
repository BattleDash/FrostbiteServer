package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = false)
public class EntryInputActionBindingsData extends Asset implements EbxPOJO {

    private long NumNetworkedAnalogInputs;
    private long NumAnalogInputs;
    private long FirstDigital;
    private long NumNetworkedDigitalInputs;
    private long NumDigitalInputs;
    private long NumInputs;
    private long SignedAnalogInputs;

}
