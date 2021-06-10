package com.example.duoduopin.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.duoduopin.fragment.ordertab.OrderDetailsFragment;
import com.example.duoduopin.fragment.ordertab.OrderMainFragment;
import com.example.duoduopin.fragment.ordertab.OrderMemberFragment;

import org.jetbrains.annotations.NotNull;

public class OrderTabAdapter extends FragmentStateAdapter {
    private Bundle bundle0;
    private Bundle bundle1;
    private Bundle bundle2;

    public OrderTabAdapter(@NotNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setBundle(Bundle bundle, int pos) {
        switch (pos) {
            case 1:
                this.bundle1 = bundle;
                break;
            case 2:
                this.bundle2 = bundle;
                break;
            default:
                this.bundle0 = bundle;
                break;
        }
    }

    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return OrderDetailsFragment.newInstance(bundle1);
            case 2:
                return OrderMemberFragment.newInstance(bundle2);
            default:
                return OrderMainFragment.newInstance(bundle0);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
