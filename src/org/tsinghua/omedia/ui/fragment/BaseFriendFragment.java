package org.tsinghua.omedia.ui.fragment;

import org.tsinghua.omedia.R;
import org.tsinghua.omedia.data.Account;
import org.tsinghua.omedia.form.AddFriendForm;
import org.tsinghua.omedia.serverAPI.AddFriendAPI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/*
 * 
 * @author liangyong
 *
 */
public class BaseFriendFragment extends BaseFragment {

	protected void showFriendInfo(final Account friend) {
		String nickname, realname, address;
		nickname = friend.getUsername();
		realname = friend.getRealName();
		address = friend.getAddress();
		AlertDialog.Builder alert = new AlertDialog.Builder(getBaseActivity());
		alert.setMessage(
				getString(R.string.friends_name) + "：" + nickname + "\n"
						+ getString(R.string.friends_realname) + "：" + realname
						+ "\n" + getString(R.string.friends_address) + "："
						+ address).setPositiveButton(
				getString(R.string.btn_addfriend),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showAddFriend(friend);
					}
				}).setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).show();

	}

	protected void showAddFriend(final Account friend) {
		AlertDialog.Builder builder;
//		AlertDialog alertDialog;
		Context mContext = getActivity();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.addfrienddailog,
				(ViewGroup) findViewById(R.id.layout_root));
		final EditText text = (EditText) layout.findViewById(R.id.editText1);
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout).setTitle(getString(R.string.btn_addfriend))
				.setPositiveButton(getString(R.string.btn_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AddFriendForm addFriendForm = new AddFriendForm();
								addFriendForm.setAccountId(dataSource
										.getAccountId());
								addFriendForm.setToken(dataSource.getToken());
								addFriendForm
										.setFriendId(friend.getAccountId());
								addFriendForm.setMsg(text.getText().toString());
								doAddFriend(addFriendForm);
							}
						}).setNegativeButton(getString(R.string.btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						}).show();
//		alertDialog = builder.create();
	}

	protected void doAddFriend(AddFriendForm form) {
		new AddFriendAPI(form, getBaseActivity()) {
			@Override
			protected void onSuccess() {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						getBaseActivity());
				alert.setTitle(getString(R.string.alertdialog_title_success))
						.setMessage(getString(R.string.addfriend_success))
						.setPositiveButton(getString(R.string.btn_yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								}).show();
			}
		}.call();

	}
}
