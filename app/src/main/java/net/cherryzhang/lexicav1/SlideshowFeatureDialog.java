package net.cherryzhang.lexicav1;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Cherry_Zhang on 16-06-03.
 * This class modularizes the slideshow feature and is used in the ItemListActivity's
 * slideshow menu item feature and also in the ItemDetailFragment's slideshow button feature.
 */
public class SlideshowFeatureDialog
{
    Dialog dialog;
    Context context;
    double timePerQuestion;
    View.OnClickListener onUserClickOkListener;
    Switch repeat;
    TextView question, barValue;
    Button okB;

    SeekBar sbar;

    public SlideshowFeatureDialog(Context context)
    {
        timePerQuestion = 0.5;

        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(context);
        }
        dialog.setTitle("Slideshow: Choose Time");
        dialog.setContentView(R.layout.set_time_dialog_slideshow);

        repeat = (Switch) dialog.findViewById(R.id.switchLoop);
        question = (TextView) dialog.findViewById(R.id.tvQuestionForTime);
        question.setText("How much time do you want for each word to be displayed in the slideshow?");
        barValue = (TextView) dialog.findViewById(R.id.tvBarValue);

        okB = (Button) dialog.findViewById(R.id.okButton);

        sbar = (SeekBar) dialog.findViewById(R.id.seekBar);
        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                timePerQuestion = progress*0.5+0.5;
                barValue.setText(timePerQuestion +" sec.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void startSlideshowFeature(View.OnClickListener onUserClickOkListener)
    {
        dialog.show();
        this.onUserClickOkListener = onUserClickOkListener;
        okB.setOnClickListener(this.onUserClickOkListener);
    }

    public void dismissDialog()
    {
        dialog.dismiss();
    }

    public boolean userClickedSlideshowShouldLoop()
    {
        return repeat.isChecked();
    }

    public double getExposureDurationPerWord()
    {
        return timePerQuestion;
    }
}
