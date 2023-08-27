package com.gruposm.chile.apppaes.object;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AnswerSheet {
    int width;
    int height;
    int numAnswers;
    double ratioMin;
    double ratioMax;
    int numBlocks;
    int optionsPerAnswers;
    int answersPerBlock;
    int correctAnswers = 0;
    int incorrectAnswers = 0;
    int percentageAnswers = 0;
    int omittedAnswers = 0;
    String tipo;
    String jsonAnswer;
    Map<Integer, String> answersLetter = new HashMap<Integer, String>();
    Map<Integer, String> answersCorrect = new HashMap<Integer, String>();
    Map<Integer, String> optionsMarkCorrect = new HashMap<Integer, String>();
    Map<Integer, String> optionsMarkUser = new HashMap<Integer, String>();
    public static String SEPARATOR = ",";
    public static String SEPARATOR_NUM_ANSWER = ":";

    public AnswerSheet(int width, int height, double ratioMin, double ratioMax) {
        this.width = width;
        this.height = height;
        this.ratioMin = ratioMin;
        this.ratioMax = ratioMax;
    }
    public void setOptionsMarkCorrects()
    {
        int sumAnswers = 0;
        int countOptions = 1;
        Map<String, Integer> indexLetter = new HashMap<String, Integer>();
        indexLetter.put("A",1);
        indexLetter.put("B",2);
        indexLetter.put("C",3);
        indexLetter.put("D",4);
        indexLetter.put("E",5);
        for(int i = 1; i <= this.getNumBlocks(); i++)
        {

            for (int k = 1; k <= this.getAnswersPerBlock(i); k++)
            {
                int indexKey = k + sumAnswers;
                String correct = this.getCorrect(indexKey);
                int index = indexLetter.get(correct);


                for (int j = 1; j <= this.getOptionsPerAnswers(); j++)
                {
                    if(j == index)
                    {
                        optionsMarkCorrect.put(countOptions,correct);
                    }
                    else
                    {
                        optionsMarkCorrect.put(countOptions,"");
                    }
                    countOptions++;

                }
            }
            sumAnswers += this.getAnswersPerBlock(i);
        }
    }
    public Map<Integer, String> geOptionMarkCorrects()
    {
        return this.optionsMarkCorrect;
    }
    public void setOptionMarkCorrect(int index, String letter)
    {
        this.optionsMarkCorrect.put(index,letter);
    }
    public String getOptionMarkCorrect(int index)
    {
        return this.optionsMarkCorrect.get(index);
    }
    public void setOptionMarkUser(int index, String letter)
    {
        this.optionsMarkUser.put(index,letter);
    }
    public String getOptionMarkUser(int index)
    {
        return this.optionsMarkUser.get(index);
    }
    public boolean checkOptionMark(int index)
    {
        String resp = this.getOptionMarkUser(index);
        String correct = this.getOptionMarkCorrect(index);
        if(resp.equals(correct))
        {
            return true;
        }
        return false;
    }
    public boolean setCorrects(String corrects)
    {
        Map<Integer, String> map = AnswerSheet.ANSWER_STRING_TO_MAP(corrects);
        if(this.getNumAnswers() < 20)
        {
            return true;
        }
        // Validar errores
        if(this.tipo == null)
        {
            return true;
        }
        if(this.tipo.equals("A"))
        {
            if(this.getNumAnswers() != 80)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 5)
            {
                return true;
            }
        }
        if(this.tipo.equals("B"))
        {
            if(this.getNumAnswers() != 55)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 4)
            {
                return true;
            }
        }
        if(this.tipo.equals("C"))
        {
            if(this.getNumAnswers() != 55)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 5)
            {
                return true;
            }
        }
        if(this.tipo.equals("D"))
        {
            if(this.getNumAnswers() != 65)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 4)
            {
                return true;
            }
        }
        if(this.tipo.equals("E"))
        {
            if(this.getNumAnswers() != 65)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 5)
            {
                return true;
            }
        }
        if(this.tipo.equals("F"))
        {
            if(this.getNumAnswers() != 80)
            {
                return true;
            }
            if(this.getOptionsPerAnswers() != 4)
            {
                return true;
            }
        }
        if(map.size() != this.getNumAnswers())
        {
            return true;
        }
        for(int i = 1; i <= this.getNumAnswers(); i++)
        {
            this.answersCorrect.put(i,map.get(i));
        }
        return false;
    }
    public void setCorrect(int index, String letter)
    {
        this.answersCorrect.put(index,letter);
    }
    public void setAnswerLetter(int index, String letter)
    {
        this.answersLetter.put(index,letter);
    }
    public String getCorrect(int index)
    {
        return this.answersCorrect.get(index);
    }
    public String getAnswer(int index)
    {
        return this.answersLetter.get(index);
    }
    public boolean checkAnswer(int index)
    {
        String resp = this.getAnswer(index);
        String correct = this.getCorrect(index);
        if(resp.equals(correct))
        {
            return true;
        }
        return false;
    }
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getNumAnswers() {
        return numAnswers;
    }

    public void setNumAnswers(int numAnswers) {
        this.numAnswers = numAnswers;
    }

    public double getRatioMin() {
        return ratioMin;
    }

    public void setRatioMin(double ratioMin) {
        this.ratioMin = ratioMin;
    }

    public double getRatioMax() {
        return ratioMax;
    }

    public void setRatioMax(double ratioMax) {
        this.ratioMax = ratioMax;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    public int getOptionsPerAnswers() {
        return optionsPerAnswers;
    }

    public void setOptionsPerAnswers(int optionsPerAnswers) {
        this.optionsPerAnswers = optionsPerAnswers;
    }

    public int getAnswersPerBlock() {
        return answersPerBlock;
    }
    public int getAnswersPerBlock(int currentBlock)
    {
        if((this.tipo.equals("B") || this.tipo.equals("C")) && currentBlock == 3)
        {
            return 15;
        }
        if((this.tipo.equals("D") || this.tipo.equals("E")) && currentBlock == 4)
        {
            return 5;
        }
        return answersPerBlock;
    }
    public void setAnswersPerBlock(int answersPerBlock) {
        this.answersPerBlock = answersPerBlock;
    }

    public Map<Integer, String> getAnswersLetter() {
        return answersLetter;
    }

    public void setAnswersLetter(Map<Integer, String> answerLetter) {
        this.answersLetter = answerLetter;
    }
    public void setResults()
    {
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        int percentageAnswers = 0;
        int omittedAnswers = 0;

        for(int i = 1; i <= this.getNumAnswers(); i++)
        {

            String answer = this.getAnswer(i);
            if(answer == null || answer.equals(""))
            {
                omittedAnswers ++;
            }
            else
            {
                boolean isCorrect = this.checkAnswer(i);
                if(isCorrect)
                {
                    correctAnswers ++;
                }
                else
                {
                    incorrectAnswers ++;
                }
            }

        }

        String[] letters = new String[this.answersLetter.size()];
        for(int i = 1; i <= this.answersLetter.size(); i++)
        {
            letters[(i-1)] = this.answersLetter.get(i);
        }
        Log.d("quiz", "respuestas:" + letters);
        Gson gson = new Gson();
        String json = gson.toJson(letters);
        Log.d("quiz", "respuestas:" + json);
        this.setJsonAnswer(json);
        this.setCorrectAnswers(correctAnswers);
        this.setIncorrectAnswers(incorrectAnswers);
        this.setOmittedAnswers(omittedAnswers);
        percentageAnswers = Math.round((correctAnswers*100)/this.getNumAnswers());
        this.setPercentageAnswers(percentageAnswers);
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public int getPercentageAnswers() {
        return percentageAnswers;
    }

    public void setPercentageAnswers(int percentageAnswers) {
        this.percentageAnswers = percentageAnswers;
    }

    public int getOmittedAnswers() {
        return omittedAnswers;
    }

    public void setOmittedAnswers(int omittedAnswers) {
        this.omittedAnswers = omittedAnswers;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public static String ANSWER_JSON_TO_STRING(JSONArray answers)
    {
        int num = 1;
        String separator = AnswerSheet.SEPARATOR;
        String[] strArray = new String[answers.length()];
        for(int i = 0; i < answers.length(); i++)
        {

            try {
                String str = num + AnswerSheet.SEPARATOR_NUM_ANSWER +answers.getString(i).toUpperCase();
                strArray[i] = str;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            num++;
        }
        return String.join(separator, strArray);
    }
    public static Map<Integer, String>  ANSWER_STRING_TO_MAP(String answers)
    {
        String[] strArray = answers.split(AnswerSheet.SEPARATOR);
        Map<Integer, String> corrects = new HashMap<Integer, String>();
        for(int i = 0; i < strArray.length; i++)
        {
            String value = strArray[i];
            String[] strData = value.split(AnswerSheet.SEPARATOR_NUM_ANSWER);
            corrects.put(Integer.parseInt(strData[0]),strData[1]);
        }
        return corrects;
    }

    public static ArrayList<RowResult> ANSWERS_TO_ROW(Map<Integer, String> corrects, String answers)
    {
        JsonArray jsonAnswers = JsonParser.parseString(answers).getAsJsonArray();
        ArrayList<RowResult> items =  new ArrayList<RowResult>();
        for(int i = 1; i <= corrects.size(); i++)
        {
            int index = (i-1);
            String letterAnswer = String.valueOf(jsonAnswers.get(index)).replaceAll("\"", "").trim().toUpperCase();
            String letterCorrect = corrects.get(i).toUpperCase();
            boolean isCorrect = false;
            if(letterAnswer.equals(letterCorrect))
            {
                isCorrect = true;
            }
            RowResult row =  new RowResult();
            row.id = i;
            row.num = i;
            row.letter = letterAnswer;
            row.isCorrect = isCorrect;
            Log.d("TAG", "isCorrect: " + isCorrect);
            items.add(row);
        }
        return items;
    }

    public String getJsonAnswer() {
        return jsonAnswer;
    }

    public void setJsonAnswer(String jsonAnswer) {
        this.jsonAnswer = jsonAnswer;
    }
}
