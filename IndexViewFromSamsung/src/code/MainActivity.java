package code;
import code.IndexViewFromSamsung.OnAlphbetTouchListener;

import com.jackyzhang.indexviewfromsamsung.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private IndexViewFromSamsung mIndexView;
	private LinearLayout ll_container;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ll_container = (LinearLayout)findViewById(R.id.ll_container);
		mIndexView = new IndexViewFromSamsung(this);
		mIndexView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		ll_container.addView(mIndexView);
		mIndexView.setOnAlphbetTouchListener(new OnAlphbetTouchListener() {
			
			@Override
			public void onAlphbetTouch(String alphbet) {
				Toast.makeText(MainActivity.this,"click on: "+alphbet,Toast.LENGTH_SHORT).show();
			}
		});
	}
}
