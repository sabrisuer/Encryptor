package com.example.mss.encryptor.logic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mss.encryptor.R;

import java.io.File;
import java.util.List;

/**
 * Created by mss on 13/01/16.
 */
public class FileAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater=null;
    private List<File> files=null;
    private Activity activity=null;

    long kilo = (long) Math.pow(2,10);
    long mega = (long) Math.pow(2,20);
    long giga = (long) Math.pow(2,30);

    static class ViewHolder{
        ImageView imageViewFileType;
        TextView textViewFileName;
        TextView textViewFileSize;
    }

    public FileAdapter(Activity activity,List<File> files){
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.files = files;
        this.activity = activity;
    }

    @Override
    public int getCount(){
        return files.size();
    }

    @Override
    public File getItem(int position){
        return files.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder viewHolder;
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.file_list_element,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.imageViewFileType = (ImageView) convertView.findViewById(R.id.image_view_file_type);
            viewHolder.textViewFileName = (TextView) convertView.findViewById(R.id.text_view_filename);
            viewHolder.textViewFileSize = (TextView) convertView.findViewById(R.id.text_view_file_size);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        File file = files.get(position);

        if(file.isDirectory())
            viewHolder.imageViewFileType.setImageResource(R.drawable.ic_folder);
        else{
            int i = file.getName().lastIndexOf('.');
            if(i>=0) {
                String extension = file.getName().substring(i,file.getName().length());
                if (extension.equals(".enc"))
                    viewHolder.imageViewFileType.setImageResource(R.drawable.ic_action_lock);
                else
                    viewHolder.imageViewFileType.setImageResource(R.drawable.ic_action_document);
            }
            else
                viewHolder.imageViewFileType.setImageResource(R.drawable.ic_action_document);

        }

        viewHolder.textViewFileName.setText(file.getName());

        if(file.isFile()){
            if(file.length() < kilo){
                viewHolder.textViewFileSize.setText(""+file.length()+"B");
            }
            else if(file.length() < mega){
                viewHolder.textViewFileSize.setText(""+(file.length()/kilo)+"KiB");
            }
            else if(file.length()< giga){
                viewHolder.textViewFileSize.setText(""+(file.length()/mega)+"MiB");
            }
            else{
                viewHolder.textViewFileSize.setText(""+(file.length()/giga)+"GiB");
            }
        }
        else
            viewHolder.textViewFileSize.setText("");

        return convertView;

    }

}
