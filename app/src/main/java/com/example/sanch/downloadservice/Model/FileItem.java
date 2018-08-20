package com.example.sanch.downloadservice.Model;

public class FileItem {

        private String mUrl;
        private String mFileName;
        private String mFileAction;
        private String mFileDest;

        public FileItem(String name ,String url ,String act, String dest){
           mFileName = name;
           mUrl = url;
           mFileAction = act;
           mFileDest = dest;
        }

        public FileItem(){
            super();
        }

        public String getUrl(){
            return mUrl;
        }

        public String getFileName() {
            return mFileName;
        }

        public String getFileAction() {
            return mFileAction;
        }

        public String getFileDest() {
        return mFileDest;
    }
}
