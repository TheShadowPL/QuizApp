import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Locale;

import org.json.*;

public class StarWars extends JFrame {

    private JLabel questionLabel;
    private JTextField answerTextField;
    private JButton generateButton;
    private JButton checkButton;
    private JLabel verificationLabel;
    private JRadioButton optionA;
    private JRadioButton optionB;
    private JRadioButton optionC;
    private ButtonGroup buttonGroup;

    private JButton languageButton;

    private ArrayList<String> people;
    private ArrayList<String> planets;
    private ArrayList<String> starships;

    private String Answercorrect;

    private ResourceBundle resourceBundle;

    public StarWars() {
        setTitle("Star Wars Quiz App");
        setSize(900, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        questionLabel = new JLabel();
        panel.add(questionLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1));

        answerTextField = new JTextField();
        answerTextField.setVisible(false);
        centerPanel.add(answerTextField);

        optionA = new JRadioButton("A");
        optionA.setVisible(false);
        centerPanel.add(optionA);

        optionB = new JRadioButton("B");
        optionB.setVisible(false);
        centerPanel.add(optionB);

        optionC = new JRadioButton("C");
        optionC.setVisible(false);
        centerPanel.add(optionC);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(optionA);
        buttonGroup.add(optionB);
        buttonGroup.add(optionC);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        checkButton = new JButton("Check Answer");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
            }
        });
        buttonsPanel.add(checkButton);

        languageButton = new JButton("PL");
        languageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLanguage();
            }
        });
        buttonsPanel.add(languageButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        generateButton = new JButton("Generate Question");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateQuestion();
            }
        });
        bottomPanel.add(generateButton, BorderLayout.WEST);

        verificationLabel = new JLabel();
        bottomPanel.add(verificationLabel, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);

        resourceBundle = ResourceBundle.getBundle("Bundle", new Locale("en", "US"));

        fetchInitialData();
    }

    private void toggleLanguage() {
        if (resourceBundle.getLocale().getLanguage().equals("en")) {
            resourceBundle = ResourceBundle.getBundle("Bundle", new Locale("pl", "PL"));
        } else {
            resourceBundle = ResourceBundle.getBundle("Bundle", new Locale("en", "US"));
        }
        updateLanguage();
    }

    private void updateLanguage() {
        generateButton.setText(resourceBundle.getString("generateButton"));
        checkButton.setText(resourceBundle.getString("checkButton"));
        languageButton.setText(resourceBundle.getString("languageButton"));
    }

    private void fetchInitialData() {
        people = fetchData("people", 5); 
        planets = fetchData("planets", 5);
        starships = fetchData("starships", 5);
    }

    private ArrayList<String> fetchData(String category, int count) { 

        ArrayList<String> data = new ArrayList<>();
        try {
            URL url = new URL("https://swapi.dev/api/" + category + "/?format=json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i = 0; i < count && i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                data.add(item.getString("name"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void generateQuestion() {
        Random random = new Random();
        int questionType = random.nextInt(2); 
        String category = getRandomCategory();
        String[] questionAndOptions;
        if (questionType == 0) {

            questionAndOptions = generateQuestionWithABC(category);
            answerTextField.setVisible(false);
            optionA.setVisible(true);
            optionB.setVisible(true);
            optionC.setVisible(true);
            System.out.println("Answercorrect: " + Answercorrect);
            System.out.println("Question and Options: " + questionAndOptions[0] + ", " + questionAndOptions[1] + ", " + questionAndOptions[2] + ", " + questionAndOptions[3]);
            optionA.setText(questionAndOptions[1]);
            optionB.setText(questionAndOptions[2]);
            optionC.setText(questionAndOptions[3]);
        } else {
            String question = generateQuestionForCategory(category);
            answerTextField.setVisible(true);
            optionA.setVisible(false);
            optionB.setVisible(false);
            optionC.setVisible(false);
            questionAndOptions = new String[]{question};
        }
        questionLabel.setText(questionAndOptions[0]);
        clearOptions();
        verificationLabel.setText("");
        answerTextField.setText("");
    }

    private String[] generateQuestionWithABC(String category) {
        String question = generateQuestionForCategory(category);
        ArrayList<String[]> options = generateOptionsForCategory(category);
        Random random = new Random();
        Collections.shuffle(options);
        String correctAnswer = "";
        String[] questionAndOptions = new String[4];
        questionAndOptions[0] = question;
        System.out.println("Question: " + question);
        for (int i = 0; i < options.size(); i++) {
            questionAndOptions[i + 1] = ((char) ('A' + i)) + ") " + options.get(i)[0];
            if (options.get(i)[1].equals("true")) {
                correctAnswer = ((char) ('A' + i)) + "";
            }
            System.out.println("Index: " + (i + 1) + ", Value: " + options.get(i)[0] + ", Correct: " + options.get(i)[1]);
        }
        System.out.println("Correct Answer: " + correctAnswer);
        return questionAndOptions;
    }

    private ArrayList<String[]> generateOptionsForCategory(String category) {
        ArrayList<String[]> options = new ArrayList<>();
        switch (category) {
            case "People":
                if (people.isEmpty()) {
                    return options;
                }
                int personIndex = new Random().nextInt(people.size());
                String person = people.get(personIndex);
                System.out.println("Answercorrect(generateOptionsForCategory): " + Answercorrect);
                options.add(new String[]{Answercorrect, "true"});
                people.remove(personIndex);
                System.out.println("Added correct answer: " + person);
                for (int i = 0; i < 2; i++) {
                    if (!people.isEmpty()) {
                        String otherPerson = people.remove(new Random().nextInt(people.size()));
                        options.add(new String[]{otherPerson, "false"});
                        System.out.println("Added false answer: " + otherPerson);
                    }
                }
                break;
            case "Planets":
                if (planets.isEmpty()) {
                    return options;
                }
                int planetIndex = new Random().nextInt(planets.size());
                String planet = planets.get(planetIndex);
                System.out.println("Answercorrect(generateOptionsForCategory): " + Answercorrect);
                options.add(new String[]{Answercorrect, "true"}); 
                planets.remove(planetIndex);
                System.out.println("Added correct answer: " + planet);
                for (int i = 0; i < 2; i++) {
                    if (!planets.isEmpty()) {
                        String otherPlanet = planets.remove(new Random().nextInt(planets.size()));
                        options.add(new String[]{otherPlanet, "false"});
                        System.out.println("Added false answer: " + otherPlanet);
                    }
                }
                break;
            case "Starships":
                if (starships.isEmpty()) {
                    return options;
                }
                int starshipIndex = new Random().nextInt(starships.size());
                String starship = starships.get(starshipIndex);
                System.out.println("Answercorrect(generateOptionsForCategory): " + Answercorrect);
                options.add(new String[]{Answercorrect, "true"}); // Poprawna odpowiedz
                starships.remove(starshipIndex);
                System.out.println("Added correct answer: " + starship);
                for (int i = 0; i < 2; i++) {
                    if (!starships.isEmpty()) {
                        String otherStarship = starships.remove(new Random().nextInt(starships.size()));
                        options.add(new String[]{otherStarship, "false"});
                        System.out.println("Added false answer: " + otherStarship);
                    }
                }
                break;
        }
        return options;
    }


    private String generateQuestionForCategory(String category) {
        Random random = new Random();
        switch (category) {
            case "People":
                if (people.isEmpty()) {
                    return "No people available for question.";
                }
                int option = random.nextInt(3);
                String person = people.get(random.nextInt(people.size()));
                Answercorrect = person;
                switch (option) {
                    case 0:
                        return "Who is the character: " + person + "?";
                    case 1:
                        return "What is the full name of the character: " + person + "?";
                    case 2:
                        return "Complete the sentence: " + person + " ...";
                    default:
                        return "Invalid option.";
                }
            case "Planets":
                if (planets.isEmpty()) {
                    return "No planets available for question.";
                }
                String planet = planets.get(random.nextInt(planets.size()));
                Answercorrect = planet;
                return "What is the name of the planet: " + planet + "?";
            case "Starships":
                if (starships.isEmpty()) {
                    return "No starships available for question.";
                }
                String starship = starships.get(random.nextInt(starships.size()));
                Answercorrect = starship;
                return "What is the name of the starship: " + starship + "?";
            default:
                return "Invalid category.";
        }
    }

    private String getRandomCategory() {
        Random random = new Random();
        String[] categories = {"People", "Planets", "Starships"};
        return categories[random.nextInt(categories.length)];
    }

    private void checkAnswer() {
        String userAnswer = "";
        if (optionA.isSelected()) {
            userAnswer = optionA.getText().substring(optionB.getText().indexOf(" ") + 1);;
        } else if (optionB.isSelected()) {
            userAnswer = optionB.getText().substring(optionB.getText().indexOf(" ") + 1);;
        } else if (optionC.isSelected()) {
            userAnswer = optionC.getText().substring(optionB.getText().indexOf(" ") + 1);;
        } else {
            System.out.println("AnswerTextField: " + answerTextField.getText().trim());
            userAnswer = answerTextField.getText().trim();
        }

        String questionLabelContent = questionLabel.getText();
        String correctAnswer = extractCorrectAnswer(questionLabelContent).trim();
        System.out.println("Correct Answer(checkAnswer): " + correctAnswer);
        System.out.println("userAnswer.equalsIgnoreCase(correctAnswer): " + userAnswer.equalsIgnoreCase(correctAnswer) + userAnswer);

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            verificationLabel.setText(resourceBundle.getString("correctMessage"));
            clearOptions();
        } else {
            verificationLabel.setText(resourceBundle.getString("incorrectMessage") + " " + correctAnswer);
            clearOptions();
        }
    }

    // metoda do wyodrebniania chcialem zrobic bardziej uniwersalna, ale nie udalo mi sie
    private String extractCorrectAnswer(String questionLabelContent) {
        System.out.println("Question Label Content: " + questionLabelContent);
        if (questionLabelContent.startsWith("Who is the character:") ||
                questionLabelContent.startsWith("What is the full name of the character:") ||
                questionLabelContent.startsWith("Complete the sentence:") ||
                questionLabelContent.startsWith("What is the name of the planet:") ||
                questionLabelContent.startsWith("What is the name of the starship:")) {
            String correctAnswer = questionLabelContent.substring(questionLabelContent.indexOf(":") + 1).replaceAll("[^a-zA-Z ]", "").trim().toLowerCase();
            System.out.println("Extracted Correct Answer: " + correctAnswer);
            return correctAnswer;
        } else {
            return "";
        }
    }


    private void clearOptions() {
        optionA.setSelected(false);
        optionB.setSelected(false);
        optionC.setSelected(false);
    }

    public static void main(String[] args) {
        new StarWars();
    }
}
