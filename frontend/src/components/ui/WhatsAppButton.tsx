export default function WhatsAppButton() {
    
  const phoneNumber = "254797779935"; 
  const message = encodeURIComponent("Hi Wild West Adventures! I have a question about a journey.");
  const url = `https://wa.me/${phoneNumber}?text=${message}`;

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className="whatsapp-fab"
      aria-label="Chat with us on WhatsApp"
    >
      <svg
        viewBox="0 0 24 24"
        width="28"
        height="24"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
        <path d="M16 13c-.3-.2-1.8-.9-2.1-1-.3-.1-.5-.2-.7.1-.2.3-.8 1-.9 1.2-.2.2-.3.2-.6.1-.3-.2-1.3-.5-2.4-1.5-.9-.8-1.5-1.8-1.7-2.1-.2-.3 0-.5.1-.6.1-.1.3-.4.5-.6.2-.2.2-.3.4-.5.1-.2.1-.4 0-.6-.1-.2-.7-1.7-1-2.3-.2-.4-.4-.4-.6-.4h-.5c-.2 0-.5.1-.7.3-.3.3-1 1-1 2.4 0 1.4 1 2.8 1.1 3 .2.2 2 3.1 4.8 4.3 2.3 1 2.8.8 3.3.7.5-.1 1.6-.6 1.8-1.2.2-.6.2-1.1.1-1.2-.1-.1-.3-.2-.6-.3z" fill="currentColor" stroke="none" />
      </svg>
    </a>
  );
}