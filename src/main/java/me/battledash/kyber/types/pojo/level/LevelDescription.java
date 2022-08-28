package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class LevelDescription implements EbxPOJO {

    private String Name;
    private String SubName;
    private String Description;

    private boolean IsSingleplayer = true;
    private boolean IsCoop = false;
    private boolean IsMenu = false;
    private boolean IsEpilogue = false;

    private LevelDescriptionComponent[] Components;

}
