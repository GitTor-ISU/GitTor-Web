import { Component, signal } from '@angular/core';
import { ZardCardComponent } from '@shared/components/z-card';

interface FaqItem {
  question: string;
  answer: string;
}

/**
 * FAQ tab — common questions about GitTor.
 */
@Component({
  selector: 'app-faq',
  imports: [ZardCardComponent],
  templateUrl: './faq.html',
})
export class Faq {
  protected readonly faqs = signal<FaqItem[]>([
    {
      question: 'How is GitTor different from GitHub or GitLab?',
      answer:
        "GitHub and GitLab host your repository on their own servers and act as the source of truth. GitTor distributes the repository itself across peers via BitTorrent — the web app mostly tracks metadata and .torrent files. If the host disappears, your repository doesn't.",
    },
    {
      question: 'What if no one is seeding a repository?',
      answer:
        'Without active seeders, peers cannot leech new copies. Most users keep at least one always-on seeder (a home server, a VPS, or the GitTor host itself). When the swarm is empty, leeching will hang until a seeder reappears.',
    },
    {
      question: 'How are commits verified across the peer network?',
      answer:
        "Each repository contains a file listing the GPG public keys of authorized contributors. The CLI verifies commit signatures against those keys, so anyone leeching can confirm that the history was signed by a recognized contributor. To add a new contributor, an existing one adds the new contributor's public key to that file.",
    },
    {
      question: 'Repository ID, magnet link, or torrent file — which should I share?',
      answer:
        "Repository IDs are shortest and don't change when the repo is updated, but they depend on the web application being online. Magnet links and torrent files work peer-to-peer without the web app, but change every time the repository is updated. The recommended workflow is repository IDs, falling back to magnet links if the host is unreachable.",
    },
    {
      question: 'Do I need to port-forward my machine?',
      answer:
        'Not strictly. GitTor will work behind NAT, but seeding is much more effective when the port set in your .gittorconfig is publicly reachable. If you can port-forward, do — it makes leeching noticeably faster for everyone else.',
    },
    {
      question: 'Can I self-host the GitTor web application?',
      answer:
        'Yes. The web app ships as a Docker Compose stack with the API, UI, PostgreSQL, and MinIO. Run tools/run-docker-prod.sh from the GitTor-Web repository to deploy. Point your CLI at it by setting api_url in .gittorconfig.',
    },
    {
      question: 'Are my repositories private?',
      answer:
        'No. Anyone with the repository ID, magnet link, or torrent file can join the swarm and read the contents. Treat published repositories as public.',
    },
  ]);
}
