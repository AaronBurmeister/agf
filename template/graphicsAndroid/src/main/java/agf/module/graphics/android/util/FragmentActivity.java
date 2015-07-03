package agf.module.graphics.android.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import agf.module.graphics.android.R;

public class FragmentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragmentactivity);
	}

	public void setFragment(Fragment newFragment, boolean addToBackStack) {
		FragmentTransaction t = getFragmentManager().beginTransaction();
		t.replace(R.id.fragments, newFragment);
		if (addToBackStack)
			t.addToBackStack(null);
		t.commit();
	}

	public void goBack() {
		if (!getFragmentManager().popBackStackImmediate())
			finish();
	}

}