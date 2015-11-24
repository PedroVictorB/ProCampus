package imd.ufrn.br.procampus.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.entities.Problem;

/**
 * Created by brunomoreira on 23/11/2015.
 */
public class UserProblemAdapter extends RecyclerView.Adapter<UserProblemAdapter.ViewHolder> {
    private List<Problem> mDataset;

    public UserProblemAdapter (List<Problem> problems) {
        mDataset = problems;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewGroup = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_problem_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(viewGroup);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.problemTitle.setText(mDataset.get(position).getTitle());

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(mDataset.get(position).getPostDate());
        holder.postDate.setText(date);

        holder.numberOfComments.setText("5");
        holder.problemDescription.setText(mDataset.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView problemTitle;
        public TextView postDate;
        public TextView numberOfComments;
        public TextView problemDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            problemTitle = (TextView) itemView.findViewById(R.id.userProblemTitle);
            postDate = (TextView) itemView.findViewById(R.id.userProblemPostDate);
            numberOfComments = (TextView) itemView.findViewById(R.id.userProblemNumberOfComments);
            problemDescription = (TextView) itemView.findViewById(R.id.userProblemDescription);
        }
    }
}