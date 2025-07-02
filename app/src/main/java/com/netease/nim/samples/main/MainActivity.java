package com.netease.nim.samples.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.netease.nim.samples.R;
import com.netease.nim.samples.base.BaseActivity;
import com.netease.nim.samples.databinding.ActivityMainBinding;
import com.netease.nim.samples.main.fragment.V2NIMLocalConversationServiceFragment;
import com.netease.nim.samples.main.fragment.V2NIMFriendServiceFragment;
import com.netease.nim.samples.main.fragment.V2NIMMessageServiceFragment;
import com.netease.nim.samples.main.fragment.V2NIMMoreFragment;
import com.netease.nim.samples.main.fragment.V2NIMUserServiceFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

	private ActivityMainBinding binding;
	private List<Fragment> fragmentList = new ArrayList<>();
	private List<Long> fragmentHashCodeList = new ArrayList<>();

	private MainViewPageAdapter mainViewPageAdapter;

	public static void startActivity(Context context){
		context.startActivity(new Intent(context, MainActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initViewPager();
		initTabLayout();
	}

	private void initTabLayout() {
		Tab tab_conversation = binding.tabLayout.newTab();
		tab_conversation.setIcon(R.mipmap.tab_conversation);
		tab_conversation.setText("会话");
		binding.tabLayout.addTab(tab_conversation);

		Tab tab_message = binding.tabLayout.newTab();
		tab_message.setIcon(R.mipmap.tab_message);
		tab_message.setText("消息");
		binding.tabLayout.addTab(tab_message);

		Tab tab_friend = binding.tabLayout.newTab();
		tab_friend.setIcon(R.mipmap.tab_friend);
		tab_friend.setText("好友");
		binding.tabLayout.addTab(tab_friend);

		Tab tab_user = binding.tabLayout.newTab();
		tab_user.setIcon(R.mipmap.tab_user);
		tab_user.setText("用户");
		binding.tabLayout.addTab(tab_user);

		Tab tab_more = binding.tabLayout.newTab();
		tab_more.setIcon(R.mipmap.tab_friend);
		tab_more.setText("更多");
		binding.tabLayout.addTab(tab_more);

		binding.tabLayout.setTabMode(TabLayout.MODE_FIXED);

		binding.tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
			@Override
			public void onTabSelected(Tab tab) {
				binding.viewPager2.setCurrentItem(tab.getPosition());
				int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.tab_selected);
				tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
				binding.title.setText(tab.getText());
			}

			@Override
			public void onTabUnselected(Tab tab) {
				int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.tab_normal);
				tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
			}

			@Override
			public void onTabReselected(Tab tab) {
				int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.tab_selected);
				tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
				binding.title.setText(tab.getText());
			}
		});
		binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
	}

	private void initViewPager() {
		fragmentList.add(new V2NIMLocalConversationServiceFragment());
		fragmentList.add(new V2NIMMessageServiceFragment());
		fragmentList.add(new V2NIMFriendServiceFragment());
		fragmentList.add(new V2NIMUserServiceFragment());
		fragmentList.add(new V2NIMMoreFragment());
		for (int i = 0; i < fragmentList.size(); i++) {
			fragmentHashCodeList.add((long) fragmentList.get(i).hashCode());
		}
		mainViewPageAdapter = new MainViewPageAdapter(this, fragmentList, fragmentHashCodeList);
		binding.viewPager2.setUserInputEnabled(false);
		binding.viewPager2.setAdapter(mainViewPageAdapter);
	}

}