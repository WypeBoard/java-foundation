package io.github.wypeboard.foundation.vcs.base.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VcsDataContainer {
    private List<PullRequest> pullRequests = new ArrayList<>();
    private Map<Integer, List<VcsComment>> commentsByPr = new HashMap<>();


}
