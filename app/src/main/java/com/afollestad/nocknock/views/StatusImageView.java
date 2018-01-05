package com.afollestad.nocknock.views;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import com.afollestad.nocknock.R;
import com.afollestad.nocknock.api.ServerStatus;

/** @author Aidan Follestad (afollestad) */
public class StatusImageView extends AppCompatImageView {

  public StatusImageView(Context context) {
    super(context);
    setStatus(ServerStatus.OK);
  }

  public StatusImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setStatus(ServerStatus.OK);
  }

  public StatusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setStatus(ServerStatus.OK);
  }

  public void setStatus(@ServerStatus.Enum int status) {
    switch (status) {
      case ServerStatus.CHECKING:
      case ServerStatus.WAITING:
        setImageResource(R.drawable.status_progress);
        setBackgroundResource(R.drawable.yellow_circle);
        break;
      case ServerStatus.ERROR:
        setImageResource(R.drawable.status_error);
        setBackgroundResource(R.drawable.red_circle);
        break;
      case ServerStatus.OK:
        setImageResource(R.drawable.status_ok);
        setBackgroundResource(R.drawable.green_circle);
        break;
    }
  }
}
