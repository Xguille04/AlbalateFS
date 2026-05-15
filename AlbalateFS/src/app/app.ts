import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// Se eliminan los import de HomeComponent, LoginComponent, etc.

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet], // El enrutador es el único necesario
  templateUrl: './app.html'
})
export class App { }