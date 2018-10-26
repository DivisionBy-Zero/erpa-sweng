package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.util.Pair;

public class MyAccountButtonAdapter extends ArrayAdapter<Pair<MyAccountButton, Drawable>> {

    private Context context;
    private List<Pair<MyAccountButton, Drawable>> myButtonDrawablesList;

    public MyAccountButtonAdapter(Context context,
                                  List<Pair<MyAccountButton, Drawable>> myButtonDrawablesList) {
        super(context, -1, myButtonDrawablesList);
        this.context = context;
        this.myButtonDrawablesList = myButtonDrawablesList;
    }

    @NonNull @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.my_account_button, parent, false);

        MyAccountButtonHolder myAccountButtonHolder = new MyAccountButtonHolder(convertView,
                position);
        myAccountButtonHolder.setDetails(myButtonDrawablesList.get(position).getFirst(),
                myButtonDrawablesList.get(position).getSecond());

        convertView.setTag(myAccountButtonHolder);

        return convertView;
    }

    public class MyAccountButtonHolder {
        private TextView text;
        private ImageView image;
        int position;

        public MyAccountButtonHolder(View v, int position) {
            text = v.findViewById(R.id.buttonName);
            image = v.findViewById(R.id.buttonImage);
            this.position = position;
        }


        public void setDetails(MyAccountButton myAccountButton, Drawable drawable) {
            text.setText(myAccountButton.getText());
            image.setImageDrawable(drawable);
        }

    }
}
