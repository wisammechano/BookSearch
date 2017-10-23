package com.recoded.booksearch;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.recoded.booksearch.databinding.ResultItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wisam on Oct 23 17.
 */

class BookListAdapter extends ArrayAdapter {
    ArrayList<Book> books;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.result_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setObject(books.get(position));
        return convertView;
    }

    public BookListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, objects);
        books = (ArrayList<Book>) objects;
    }

    public void resetCollection(ArrayList<Book> books){
        this.books = books;
    }

    private class ViewHolder {
        ResultItemBinding binding;

        public ViewHolder(View itemView) {
            binding = DataBindingUtil.bind(itemView);
        }

        public void setObject(Book book) {
            binding.setBook(book);
        }
    }
}
