package com.lateralthoughts.vue.pendingaisles;

public class Colors {
    String [] colors ={"#99FFCC","#CCCC99","#CCCCCC","#CCCCFF","#CCFF99","#CCFFCC","#CCFFFF","#FFCCCC","#FFCCFF","#FFFF99","#FFFFCC"};
  public String getColorCode(int position){
    int pos = position%colors.length;
      return colors[pos];
      
  }
}
