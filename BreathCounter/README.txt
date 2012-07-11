The "breathTest.xml" form will launch the BreathCount application 
to obtain the breaths per minute of a subject calculated from 
observations taken over a 30-second interval.

This is an example of an external app being invoked by ODK Collect 1.2.
External apps can be invoked to return string, integer and decimal 
values. To do so, the appearance attribute for the <input> tag should be
"ex:" followed by the intent name for launching that app. In this case, the 
<input> declaration is:

    <input appearance="ex:change.uw.android.BREATHCOUNT" ref="/form/breathCount" >
	  <label ref="jr:itext('breathCountMsg')"/>
	</input>

So the intent name to launch the BreathCount app is "change.uw.android.BREATHCOUNT"
This is defined in the AndroidManifest.xml of this project as being handled by
the change.uw.breathcounter.BreathCountActivity class.

The value returned in the "value" extra ( intent.putExtra("value", mAnswer) ) is 
the value stored in the form when control returns to ODK Collect.

Copy, rename the application package, and change the intent name to create
your own custom external app and/or widget for your own needs.
