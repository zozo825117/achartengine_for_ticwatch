package com.fusion.types;

// project globals structure
public class ProjectGlobals extends Types {
    volatile quaternion QuaternionPacketType;	// quaternion transmitted over UART
    quaternion DefaultQuaternionPacketType;	// default quaternion transmitted at power on
    public int SamplingEventStruct;				// MQX-Lite hardware timer event
    public boolean RunKFEventStruct;				// MQX-Lite kalman filter sensor fusion event
    public int MagCalEventStruct;				// MQX-Lite magnetic calibration event
    public int loopcounter;								// counter incrementing each iteration of sensor fusion (typically 25Hz)
    public boolean AngularVelocityPacketOn;			// flag to transmit angular velocity packet
    public boolean DebugPacketOn;					// flag to transmit debug packet
    public boolean RPCPacketOn;						// flag to transmit roll, pitch, compass packet
    public boolean AltPacketOn;						// flag to transmit altitude packet
}
