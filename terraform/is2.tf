provider "google" {
    credentials = file("~/code/credentials/i3s-dev-b538d40535d9.json")
    project = "i3s-dev"
    region = "europe-west1"
    zone = "europe-west1-a"
}

resource "google_sql_database_instance" "master" {
  name             = "master-instance"
  database_version = "POSTGRES_11"
  region           = "us-central1"

  settings {
      tier = "db-f1-micro"
  }
}