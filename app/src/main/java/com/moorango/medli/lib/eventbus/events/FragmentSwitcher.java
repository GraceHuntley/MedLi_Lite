package com.moorango.medli.lib.eventbus.events;

import com.moorango.medli.lib.NavConstants;

/**
 * Created by cmac147 on 7/16/15.
 */
public class FragmentSwitcher extends Event {

    private NavConstants.FRAGMENT_DATA mFragment;

    public FragmentSwitcher(NavConstants.FRAGMENT_DATA fragment) {
        mFragment = fragment;
    }


    public NavConstants.FRAGMENT_DATA getFragment() {
        return mFragment;
    }
}
