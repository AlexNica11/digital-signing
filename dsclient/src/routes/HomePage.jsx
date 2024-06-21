const HomePage = () => {
    return(
        <div>
            <h3>Welcome to Digital Signatures</h3>
            <h4>Capabilities:</h4>
            <ul className="collection">
                <li className="collection-item">Sign Document - Signing for PAdES, XAdES, JAdES, CAdES at baseline levels B, T, LT, LTA</li>
                <li className="collection-item">Document Status - Real time status for job signatures</li>
                <li className="collection-item">Profile - Profile and key stores information</li>
            </ul>
        </div>
    );
};

export default HomePage;
