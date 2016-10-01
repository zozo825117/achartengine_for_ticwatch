package com.fusion.types;

/**
 * Created by zozo on 2016/9/27.
 */
// gyro sensor classure definition
public class PressureSensor {
    int iH;				// most recent unaveraged height (counts)
    int iP;				// most recent unaveraged pressure (counts)
    float fH;				// most recent unaveraged height (m)
    float fT;				// most recent unaveraged temperature (C)
    float fmPerCount;		// meters per count
    float fCPerCount;		// degrees Celsius per count
    int iT;				// most recent unaveraged temperature (counts)
    int iWhoAmI;			// sensor whoami
}
