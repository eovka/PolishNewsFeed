package pl.pisze_czytam.polishnews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<News> {
    public NewsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<News> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View newsItem = convertView;

        class ViewHolder {
            private TextView title;
            private TextView author;
            private TextView date;
            private TextView section;
            private ImageView image;
            private TextView trailer;
            private View newsContainer;
        }

        ViewHolder holder;
        if (convertView == null) {
            newsItem = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
            holder = new ViewHolder();
            holder.title = newsItem.findViewById(R.id.title_view);
            holder.author = newsItem.findViewById(R.id.author_view);
            holder.date = newsItem.findViewById(R.id.date_view);
            holder.section = newsItem.findViewById(R.id.section_view);
            holder.image = newsItem.findViewById(R.id.image_view);
            holder.trailer = newsItem.findViewById(R.id.trailer_view);
            holder.newsContainer = newsItem.findViewById(R.id.news_container);
            newsItem.setTag(holder);
        } else {
            holder = (ViewHolder) newsItem.getTag();
        }

        News currentNews = getItem(position);
        holder.title.setText(currentNews.getTitle());
        holder.author.setText(currentNews.getAuthor());
        holder.date.setText(currentNews.getDate());
        holder.section.setText(currentNews.getSection());
        holder.image.setImageDrawable(currentNews.getImage());
        holder.trailer.setText(currentNews.getTrailer());
        holder.newsContainer.setBackgroundColor(currentNews.getColorBackground());

        if (currentNews.hasImage()) {
            holder.image.setImageDrawable(currentNews.getImage());
            holder.image.setVisibility(View.VISIBLE);
        } else {
            holder.image.setVisibility(View.GONE);
        }
        return newsItem;
    }
}
