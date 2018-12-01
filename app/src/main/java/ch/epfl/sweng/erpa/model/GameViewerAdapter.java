package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;

public class GameViewerAdapter extends BaseExpandableListAdapter {

    @BindView(R.id.titleTextView) TextView title;
    @BindView(R.id.descriptionTextView) TextView description;
    @BindView(R.id.gmTextView) TextView gmName;
    @BindView(R.id.universeTextView) TextView universe;
    @BindView(R.id.difficultyTextView) TextView difficulty;
    @BindView(R.id.oneShotOrCampaignTextView) TextView type;
    @BindView(R.id.sessionNumberTextView) TextView numSessions;
    @BindView(R.id.sessionLengthTextView) TextView sessionLength;
    @BindView(R.id.participatingPlayersTextView) TextView playersInGame;
    private Context mContext;
    private Game mGame;
    private List<String> mListDataHeader;
    private HashMap<String, LinearLayout> mListChildData;

    public GameViewerAdapter(Context context, Game game, List<String> listDataHeader,
                             HashMap<String, LinearLayout> listChildData) {
        mContext = context;
        mGame = game;
        mListDataHeader = listDataHeader;
        mListChildData = listChildData;
    }

    private void setGameInfoBody() {
        title.setText(mGame.getName());
        description.setText(mGame.getDescription());
        gmName.setText(mGame.getGmUuid());
        universe.setText(mGame.getUniverse());
        difficulty.setText(mGame.getDifficulty().toString());
        type.setText(mGame.getOneshotOrCampaign().toString());

        String numSessionsString = mGame.getNumberSessions().map(Object::toString).orElse(
                "Unspecified");
        numSessions.setText(numSessionsString);

        String mGameLength = mGame.getSessionLengthInMinutes().map(Object::toString).orElse(
                "Unspecified");
        sessionLength.setText(mGameLength);

        String playerInfo = Stream.of(mGame.getPlayersUuid()).reduce("",
                (elem, acc) -> acc + ", " + elem);
        playersInGame.setText(playerInfo);
    }

    @Override public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override public int getChildrenCount(int groupPosition) {
        LinearLayout layout = getChild(groupPosition, 0);
        return layout == null ? 1 : layout.getChildCount();
    }

    @Override public String getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override public LinearLayout getChild(int groupPosition, int childPosition) {
        return mListChildData.get(getGroup(groupPosition));
    }

    @Override public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override public boolean hasStableIds() {
        return false;
    }

    @Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                       ViewGroup parent) {
        String headerTitle = getGroup(groupPosition);

        if (convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.game_viewer_expandable_list_group, null);

        TextView groupTitle = convertView.findViewById(R.id.gameViewerExpandableListGroup);
        groupTitle.setText(headerTitle);
        return convertView;
    }

    @Override public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                       View convertView, ViewGroup parent) {
        if (convertView == null) convertView = getChild(groupPosition, childPosition);
        if (groupPosition == 0) {
            ButterKnife.bind(this, convertView);
            setGameInfoBody();
        }
        return convertView;
    }

    @Override public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
