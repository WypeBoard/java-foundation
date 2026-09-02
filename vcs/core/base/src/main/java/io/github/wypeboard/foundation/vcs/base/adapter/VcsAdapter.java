package io.github.wypeboard.foundation.vcs.base.adapter;

import io.github.wypeboard.foundation.vcs.base.model.VcsDataContainer;

public interface VcsAdapter {

    VcsDataContainer fetchPullRequests();

    VcsDataContainer fetchComments();
}
