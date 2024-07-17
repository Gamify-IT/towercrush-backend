-- liquibase formatted sql

-- changeset leon:change1-1
CREATE TABLE "configuration_questions" ("configuration_id" UUID NOT NULL, "questions_id" UUID NOT NULL, CONSTRAINT "configuration_questions_pkey" PRIMARY KEY ("configuration_id", "questions_id"));

-- changeset leon:change1-2
CREATE TABLE "game_teams" ("game_id" UUID NOT NULL, "teams_id" UUID NOT NULL, "teams_key" VARCHAR(255) NOT NULL, CONSTRAINT "game_teams_pkey" PRIMARY KEY ("game_id", "teams_key"));

-- changeset leon:change1-3
CREATE TABLE "round_team_votes" ("round_id" UUID NOT NULL, "team_votes_id" UUID NOT NULL, "team_votes_key" VARCHAR(255) NOT NULL, CONSTRAINT "round_team_votes_pkey" PRIMARY KEY ("round_id", "team_votes_key"));

-- changeset leon:change1-4
ALTER TABLE "configuration_questions" ADD CONSTRAINT "uk_87jmj05cn4rqb8wfq6qxej42w" UNIQUE ("questions_id");

-- changeset leon:change1-5
ALTER TABLE "game_teams" ADD CONSTRAINT "uk_p26de1kitkwtwwtlvj9ee8ite" UNIQUE ("teams_id");

-- changeset leon:change1-6
ALTER TABLE "round_team_votes" ADD CONSTRAINT "uk_su7v172db5fld04qiv3af355q" UNIQUE ("team_votes_id");

-- changeset leon:change1-7
CREATE SEQUENCE  IF NOT EXISTS "hibernate_sequence" AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset leon:change1-8
CREATE TABLE "configuration" ("id" UUID NOT NULL, CONSTRAINT "configuration_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-9
CREATE TABLE "game" ("id" UUID NOT NULL, "configuration_id" UUID, "initial_tower_size" BIGINT NOT NULL, "lobby_name" VARCHAR(255), "started_game" TIMESTAMP WITHOUT TIME ZONE, "winner_team" VARCHAR(255), CONSTRAINT "game_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-10
CREATE TABLE "game_answer_points" ("game_id" UUID NOT NULL, "answer_points" INTEGER, "answer_points_key" VARCHAR(255) NOT NULL, CONSTRAINT "game_answer_points_pkey" PRIMARY KEY ("game_id", "answer_points_key"));

-- changeset leon:change1-11
CREATE TABLE "game_correct_answer_count" ("game_id" UUID NOT NULL, "correct_answer_count" INTEGER, "correct_answer_count_key" VARCHAR(255) NOT NULL, CONSTRAINT "game_correct_answer_count_pkey" PRIMARY KEY ("game_id", "correct_answer_count_key"));

-- changeset leon:change1-12
CREATE TABLE "game_current_question" ("game_id" UUID NOT NULL, "current_question" INTEGER, "current_question_key" VARCHAR(255) NOT NULL, CONSTRAINT "game_current_question_pkey" PRIMARY KEY ("game_id", "current_question_key"));

-- changeset leon:change1-13
CREATE TABLE "game_result" ("id" BIGINT NOT NULL, "configuration_asuuid" UUID, "played_time" TIMESTAMP WITHOUT TIME ZONE, "player_id" VARCHAR(255), "question_count" INTEGER NOT NULL, "score" FLOAT4 NOT NULL, CONSTRAINT "game_result_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-14
CREATE TABLE "game_result_correct_answered_questions" ("game_result_id" BIGINT NOT NULL, "correct_answered_questions_id" UUID NOT NULL);

-- changeset leon:change1-15
CREATE TABLE "game_result_wrong_answered_questions" ("game_result_id" BIGINT NOT NULL, "wrong_answered_questions_id" UUID NOT NULL);

-- changeset leon:change1-16
CREATE TABLE "game_rounds" ("game_id" UUID NOT NULL, "rounds_id" UUID NOT NULL);

-- changeset leon:change1-17
CREATE TABLE "game_tower_size" ("game_id" UUID NOT NULL, "tower_size" INTEGER, "tower_size_key" VARCHAR(255) NOT NULL, CONSTRAINT "game_tower_size_pkey" PRIMARY KEY ("game_id", "tower_size_key"));

-- changeset leon:change1-18
CREATE TABLE "player" ("id" UUID NOT NULL, "key" UUID, "player_name" VARCHAR(255), CONSTRAINT "player_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-19
CREATE TABLE "question" ("id" UUID NOT NULL, "right_answer" VARCHAR(255), "text" VARCHAR(255), CONSTRAINT "question_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-20
CREATE TABLE "question_wrong_answers" ("question_id" UUID NOT NULL, "wrong_answers" VARCHAR(255));

-- changeset leon:change1-21
CREATE TABLE "round" ("id" UUID NOT NULL, "question_id" UUID, CONSTRAINT "round_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-22
CREATE TABLE "round_result" ("id" UUID NOT NULL, "answer" VARCHAR(255), "question_id" UUID, CONSTRAINT "round_result_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-23
CREATE TABLE "round_team_ready_for_next_question" ("round_id" UUID NOT NULL, "team_ready_for_next_question" BOOLEAN, "team_ready_for_next_question_key" VARCHAR(255) NOT NULL, CONSTRAINT "round_team_ready_for_next_question_pkey" PRIMARY KEY ("round_id", "team_ready_for_next_question_key"));

-- changeset leon:change1-24
CREATE TABLE "team" ("id" UUID NOT NULL, CONSTRAINT "team_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-25
CREATE TABLE "team_players" ("team_id" UUID NOT NULL, "players_id" UUID NOT NULL, CONSTRAINT "team_players_pkey" PRIMARY KEY ("team_id", "players_id"));

-- changeset leon:change1-26
CREATE TABLE "team_votes" ("id" UUID NOT NULL, CONSTRAINT "team_votes_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-27
CREATE TABLE "team_votes_votes" ("team_votes_id" UUID NOT NULL, "votes_id" UUID NOT NULL);

-- changeset leon:change1-28
CREATE TABLE "vote" ("id" UUID NOT NULL, "answer" VARCHAR(255), "player_id" UUID, CONSTRAINT "vote_pkey" PRIMARY KEY ("id"));

-- changeset leon:change1-29
ALTER TABLE "round_team_ready_for_next_question" ADD CONSTRAINT "fk26md1yr7emtp0ox7e7n894ugp" FOREIGN KEY ("round_id") REFERENCES "round" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-30
ALTER TABLE "game_result_correct_answered_questions" ADD CONSTRAINT "fk2yr4n6edjx6h62qhjfj0x0n9h" FOREIGN KEY ("correct_answered_questions_id") REFERENCES "round_result" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-31
ALTER TABLE "game_rounds" ADD CONSTRAINT "fk378y0jg9i07kroelcx5qif7ww" FOREIGN KEY ("rounds_id") REFERENCES "round" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-32
ALTER TABLE "vote" ADD CONSTRAINT "fk5cxvn2h3fuqpbgvedhp3nip49" FOREIGN KEY ("player_id") REFERENCES "player" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-33
ALTER TABLE "game_result_wrong_answered_questions" ADD CONSTRAINT "fk5l5weg5gvyjdyjutrqiigreqc" FOREIGN KEY ("wrong_answered_questions_id") REFERENCES "round_result" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-34
ALTER TABLE "game_teams" ADD CONSTRAINT "fk6jh4evaf5i1ww49qkod6aih7c" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-35
ALTER TABLE "game_correct_answer_count" ADD CONSTRAINT "fk6tlgfhmb84edimj1yifat0g3v" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-36
ALTER TABLE "question_wrong_answers" ADD CONSTRAINT "fk9thusvh2s8wjgxjf3gkwr7bnu" FOREIGN KEY ("question_id") REFERENCES "question" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-37
ALTER TABLE "round_team_votes" ADD CONSTRAINT "fka567niijnrpctxxiimats7kyb" FOREIGN KEY ("round_id") REFERENCES "round" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-38
ALTER TABLE "team_votes_votes" ADD CONSTRAINT "fkaxs6fkvdsal1ruul0y0rhm459" FOREIGN KEY ("votes_id") REFERENCES "vote" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-39
ALTER TABLE "team_votes_votes" ADD CONSTRAINT "fkbf90ijkp6lltfqt82ltnaffth" FOREIGN KEY ("team_votes_id") REFERENCES "team_votes" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-40
ALTER TABLE "game_result_wrong_answered_questions" ADD CONSTRAINT "fkbmqsqjwwrhconh1qhvfv7nyta" FOREIGN KEY ("game_result_id") REFERENCES "game_result" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-41
ALTER TABLE "team_players" ADD CONSTRAINT "fkc9igy2kys82rwa80px3q0usqa" FOREIGN KEY ("team_id") REFERENCES "team" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-42
ALTER TABLE "game_teams" ADD CONSTRAINT "fkdsw3km1ahgoqfhfra1ts1bauq" FOREIGN KEY ("teams_id") REFERENCES "team" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-43
ALTER TABLE "configuration_questions" ADD CONSTRAINT "fkewy22y8x7me09uka66yaovavm" FOREIGN KEY ("questions_id") REFERENCES "question" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-44
ALTER TABLE "game_tower_size" ADD CONSTRAINT "fkg4kl0tb3w8sm27g1m02dbgmsr" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-45
ALTER TABLE "game_current_question" ADD CONSTRAINT "fkg8s1o8vkf5fdd125bwga56qi9" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-46
ALTER TABLE "game_answer_points" ADD CONSTRAINT "fkhg99qdvpce4nxpi10iwxxafns" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-47
ALTER TABLE "game_rounds" ADD CONSTRAINT "fkhwg9acgvm267aqpi5wj3wm54h" FOREIGN KEY ("game_id") REFERENCES "game" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-48
ALTER TABLE "round" ADD CONSTRAINT "fkmlbyw7m6kgtm472xr5r4bnh1m" FOREIGN KEY ("question_id") REFERENCES "question" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-49
ALTER TABLE "round_result" ADD CONSTRAINT "fknbh8yrgf47myl1mfiv2johows" FOREIGN KEY ("question_id") REFERENCES "question" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-50
ALTER TABLE "team_players" ADD CONSTRAINT "fkoh3nyypbfjy208372s2aq12ei" FOREIGN KEY ("players_id") REFERENCES "player" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-51
ALTER TABLE "configuration_questions" ADD CONSTRAINT "fkpuxg1dtbsi0no6cj8ynv0f8tt" FOREIGN KEY ("configuration_id") REFERENCES "configuration" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-52
ALTER TABLE "round_team_votes" ADD CONSTRAINT "fkqujtwb0n3c99obl8mxdvv0s3v" FOREIGN KEY ("team_votes_id") REFERENCES "team_votes" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset leon:change1-53
ALTER TABLE "game_result_correct_answered_questions" ADD CONSTRAINT "fkrf30lgepnva24yiwi6en9oedc" FOREIGN KEY ("game_result_id") REFERENCES "game_result" ("id") ON UPDATE NO ACTION ON DELETE NO ACTION;

