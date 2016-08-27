package org.zankio.cculife.ui.base.helper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;


public class FragmentPagerHelper {
    private final FragmentManager fragmentManager;
    private final Page[] pages;
    private ViewPager mViewPager;

    public FragmentPagerHelper(@NonNull FragmentManager fragmentManager, @NonNull Page[] pages) {
        this.fragmentManager = fragmentManager;
        this.pages = pages;
    }

    public void setupViewPager(@NonNull ViewPager viewPager) {
        this.mViewPager = viewPager;
        this.mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager));
    }

    @Nullable
    public Fragment getFragment(int position) {
        if (position >= pages.length) return null;
        return fragmentManager.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + position);
    }

    public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public FragmentPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) { return pages[position].getFragment(); }

        @Override
        public int getCount() { return pages.length; }

        @Override
        public CharSequence getPageTitle(int position) { return pages[position].title; }
    }

    public static class Page {
        public String title;
        public Class<? extends Fragment> fragment;
        private Bundle argument;

        public Page (String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment.getClass();
            this.argument = fragment.getArguments();
        }

        public Fragment getFragment() {
            Fragment fragment = null;
            try {
                fragment = this.fragment.newInstance();
                fragment.setArguments(this.argument);
            }
            catch (InstantiationException e) { e.printStackTrace(); }
            catch (IllegalAccessException e) { e.printStackTrace(); }

            return  fragment;
        }
    }
}
