/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package android.labstreaminglayer;

public final class processing_options_t {
  public final static processing_options_t post_none = new processing_options_t("post_none", lslAndroidJNI.post_none_get());
  public final static processing_options_t post_clocksync = new processing_options_t("post_clocksync", lslAndroidJNI.post_clocksync_get());
  public final static processing_options_t post_dejitter = new processing_options_t("post_dejitter", lslAndroidJNI.post_dejitter_get());
  public final static processing_options_t post_monotonize = new processing_options_t("post_monotonize", lslAndroidJNI.post_monotonize_get());
  public final static processing_options_t post_threadsafe = new processing_options_t("post_threadsafe", lslAndroidJNI.post_threadsafe_get());
  public final static processing_options_t post_ALL = new processing_options_t("post_ALL", lslAndroidJNI.post_ALL_get());

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static processing_options_t swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + processing_options_t.class + " with value " + swigValue);
  }

  private processing_options_t(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private processing_options_t(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private processing_options_t(String swigName, processing_options_t swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static final processing_options_t[] swigValues = { post_none, post_clocksync, post_dejitter, post_monotonize, post_threadsafe, post_ALL };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

