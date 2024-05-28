# JJeopardy

JJeopardy is game of questions and answers. Players are offered to choose and answer 
questions from a number of categories, and their successful answers are awarded with points.
At the end of the game, the player with the most points - wins.

The application is written in Java and requires Java runtime to run ([download](https://www.java.com/download/)).

Please, note that JJeopardy is not affiliated with Jeopardy!® or Sony Pictures Digital Inc.

## The Game

The game supports up to 10 categories (columns) and up to 10 questions per category, so total of up to 100 of questions. Each question
can contain a text field and an image for each - the question and the answer.

<img width="1200" alt="Game table" src="https://github.com/mrzhenya/jjeopardy/assets/9154225/8e75215a-05fd-4b46-a71a-e77821970ac9">

## Game files

Games could be created in the app or loaded from xml of html files. The format of the xml file is described here 
([XML game format](https://github.com/mrzhenya/jjeopardy/wiki/XML-game-format)); however, it is unlikely that
you'd need to edit an XMl file since all necessary edit functionality is already present in the app.

The html files are supported only the ones downloaded from the [JeopardyLabs.com](https://jeopardylabs.com/))
site. After loading a JeopardyLabs.com game in JJeopardy, please verify that all images download properly and
replace images that don't show up in the JJeopardy game UI (the edit game window).

Any game could be added to the JJeopardy game library to be played at a later time. You can also edit or print
any game you load or that's already present in the game library.

<img width="1200" alt="Edit game" src="https://github.com/mrzhenya/jjeopardy/assets/9154225/15105130-8cb7-4805-b946-710bf7e4f2e4">

## JeopardyLabs

Even though, this project is not affiliated with the [jeopardylabs.com](https://jeopardylabs.com), it supports loading games downloaded
from that site. To run a jeopardylabs.com game in JJeopardy, you simply need to download an HTML file from
jeopardylabs.com and load the downloaded HTML file into JJeopardy using the Load game option and add it to 
the JJeopardy game library. After that, you will be able to edit, print, or play the game.

<img width="1200" alt="JeopardyLabs download" src="https://github.com/mrzhenya/jjeopardy/assets/9154225/e407a9b7-7dd6-4dfc-837c-6e7aa54b88ab">
