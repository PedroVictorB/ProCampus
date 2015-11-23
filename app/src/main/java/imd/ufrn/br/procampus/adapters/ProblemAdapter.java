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
 * Created by brunomoreira on 22/11/2015.
 */
public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ViewHolder>{
    private List<Problem> mDataset;

    public ProblemAdapter (List<Problem> problems) {
        mDataset = problems;
    }

    @Override
    public ProblemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewGroup = LayoutInflater.from(parent.getContext()).inflate(R.layout.problem_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(viewGroup);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ProblemAdapter.ViewHolder holder, int position) {
        holder.userImage.setImageBitmap(mDataset.get(position).getUser().getImage());
        holder.username.setText(mDataset.get(position).getUser().getName());
        holder.problemTitle.setText(mDataset.get(position).getTitle());

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(mDataset.get(position).getPostDate());
        holder.postDate.setText(date);

        holder.hasImage.setImageBitmap(mDataset.get(position).getImage());
        holder.numberOfComments.setText(mDataset.get(position).getDescription());
        holder.problemDescription.setText(mDataset.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImage;
        public TextView username;
        public TextView problemTitle;
        public TextView postDate;
        public ImageView hasImage;
        public TextView numberOfComments;
        public TextView problemDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            userImage = (ImageView) itemView.findViewById(R.id.userImage);
            username = (TextView) itemView.findViewById(R.id.username);
            problemTitle = (TextView) itemView.findViewById(R.id.problemTitle);
            postDate = (TextView) itemView.findViewById(R.id.postDate);
            hasImage = (ImageView) itemView.findViewById(R.id.hasImage);
            numberOfComments = (TextView) itemView.findViewById(R.id.numberOfComments);
            problemDescription = (TextView) itemView.findViewById(R.id.problemDescription);
        }
    }
}
